package sk.emaa.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.AttendanceDto;
import sk.emaa.dto.AttendanceItemDto;
import sk.emaa.dto.StudentAttendanceDto;
import sk.emaa.dto.TrainingAttendanceDto;
import sk.emaa.dto.TrainingDto;
import sk.emaa.model.entity.tables.Attendance;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.MartialArt;
import sk.emaa.model.entity.tables.Student;
import sk.emaa.model.entity.tables.StudentMartialArt;
import sk.emaa.model.entity.tables.Training;
import sk.emaa.model.entity.tables.records.TrainingRecord;
import sk.emaa.util.AppConstants;

@Service
@RequiredArgsConstructor
public class AttendanceService {
	
	private DateTimeFormatter trainingDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	
	private final DSLContext dsl;

	public void addTraining(TrainingDto training) throws IllegalStateException {
	    dsl.transaction(configuration -> {
	        DSLContext dsl = DSL.using(configuration);
	        
	        boolean exists = dsl.fetchExists(
	            dsl.selectOne()
	               .from(Training.TRAINING)
	               .where(Training.TRAINING.SCHOOL_ID.eq(training.schoolId()))
	               .and(Training.TRAINING.DATE.eq(training.date()))
	               .and(Training.TRAINING.MARTIAL_ART_ID.eq(training.martialArtId()))
	        );

	        if (exists) {
	            throw new IllegalStateException("Tréning s dátumom " + training.date().format(trainingDateFormatter) + " už existuje");
	        }

	        TrainingRecord trainingRecord = dsl.insertInto(Training.TRAINING)
	            .set(Training.TRAINING.SCHOOL_ID, training.schoolId())
	            .set(Training.TRAINING.DATE, training.date())
	            .set(Training.TRAINING.MARTIAL_ART_ID, training.martialArtId())
	            .returning(Training.TRAINING.ID)
	            .fetchOne();

	        if (trainingRecord == null) {
	            throw new IllegalStateException("Failed to create training record");
	        }

	        int trainingId = trainingRecord.getId();

	        dsl.insertInto(Attendance.ATTENDANCE,
	                Attendance.ATTENDANCE.STUDENT_ID,
	                Attendance.ATTENDANCE.TRAINING_ID,
	                Attendance.ATTENDANCE.PRESENT)
	            .select(
	                dsl.select(
	                        Student.STUDENT.ID,
	                        DSL.val(trainingId),
	                        DSL.val(false)
	                    )
	                    .from(Student.STUDENT)
	                    .join(StudentMartialArt.STUDENT_MARTIAL_ART)
	                        .on(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(Student.STUDENT.ID))
	                    .where(Student.STUDENT.SCHOOL_ID.eq(training.schoolId()))
	                    .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(training.martialArtId()))
	                    .and(StudentMartialArt.STUDENT_MARTIAL_ART.ACTIVE.eq(true))
	                    .and(Student.STUDENT.ACTIVE.eq(true))
	            )
	            .execute();
	    });
	}
	
	@Transactional
	public void deleteTraining(int trainingId) {

	    // 1. Načítaj všetkých študentov ktorí boli prítomní a platia kreditom
	    // Teraz čítame payment_type a credit zo STUDENT_MARTIAL_ART cez join na TRAINING
	    var records = dsl.select(
	                Student.STUDENT.ID,
	                StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT,
	                StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID
	            )
	            .from(Attendance.ATTENDANCE)
	            .join(Student.STUDENT)
	                .on(Student.STUDENT.ID.eq(Attendance.ATTENDANCE.STUDENT_ID))
	            .join(Training.TRAINING)
	                .on(Training.TRAINING.ID.eq(Attendance.ATTENDANCE.TRAINING_ID))
	            .join(StudentMartialArt.STUDENT_MARTIAL_ART)
	                .on(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(Student.STUDENT.ID)
	                    .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(Training.TRAINING.MARTIAL_ART_ID)))
	            .where(Attendance.ATTENDANCE.TRAINING_ID.eq(trainingId))
	            .and(Attendance.ATTENDANCE.PRESENT.isTrue())
	            .and(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE.eq(AppConstants.paymentType_credit))
	            .fetch();

	    // 2. Vráť kredit každému takému študentovi
	    for (var r : records) {
	        int studentId = r.get(Student.STUDENT.ID);
	        int credit = r.get(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT);
	        int martialArtId = r.get(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID);

	        dsl.update(StudentMartialArt.STUDENT_MARTIAL_ART)
	           .set(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT, credit + 1)
	           .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
	           .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
	           .execute();
	    }

	    // 3. Zmaž tréning (cascade zmaže attendance aj credit_transaction)
	    dsl.deleteFrom(Training.TRAINING)
	       .where(Training.TRAINING.ID.eq(trainingId))
	       .execute();
	}

	
	public AttendanceDto getAttendance(int schoolId, int month, int year, int martialArtId) {

	    YearMonth yearMonth = YearMonth.of(year, month);
	    LocalDate start = yearMonth.atDay(1);
	    LocalDate end = yearMonth.atEndOfMonth();
	    
	    String color = dsl.select(MartialArt.MARTIAL_ART.COLOR)
	        .from(MartialArt.MARTIAL_ART)
	        .where(MartialArt.MARTIAL_ART.ID.eq(martialArtId))
	        .fetchOne(MartialArt.MARTIAL_ART.COLOR);

	    var records = dsl.select(
	            Student.STUDENT.ID.as("student_id"),
	            Student.STUDENT.FIRSTNAME,
	            Student.STUDENT.LASTNAME,
	            Student.STUDENT.STUDENT_TYPE,
	            StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE,
	            StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT,
	            StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT,
	            Training.TRAINING.ID.as("training_id"),
	            Training.TRAINING.DATE.as("training_date"),
	            Attendance.ATTENDANCE.ID.as("attendance_id"),
	            Attendance.ATTENDANCE.PRESENT
	        )
	        .from(Student.STUDENT)
	        .join(Attendance.ATTENDANCE)
	            .on(Attendance.ATTENDANCE.STUDENT_ID.eq(Student.STUDENT.ID))
	        .join(Training.TRAINING)
	            .on(Training.TRAINING.ID.eq(Attendance.ATTENDANCE.TRAINING_ID))
	        .join(StudentMartialArt.STUDENT_MARTIAL_ART)
	            .on(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(Student.STUDENT.ID)
	                .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId)))
	        .where(Student.STUDENT.SCHOOL_ID.eq(schoolId))
	        .and(Training.TRAINING.DATE.between(start, end))
	        .and(Training.TRAINING.MARTIAL_ART_ID.eq(martialArtId))
	        .and(Student.STUDENT.ACTIVE.isTrue())
	        .orderBy(Student.STUDENT.LASTNAME.asc(), Training.TRAINING.DATE.asc())
	        .fetch();

	    Map<Integer, Map<LocalDate, AttendanceItemDto>> attendanceMap = new LinkedHashMap<>();
	    Map<Integer, Integer> studentIds = new HashMap<>();
	    Map<Integer, String> firstnames = new HashMap<>();
	    Map<Integer, String> lastnames = new HashMap<>();
	    Map<Integer, Integer> credits = new HashMap<>();
	    Map<Integer, String> studentTypes = new HashMap<>();
	    Map<Integer, String> paymentTypes = new HashMap<>();
	    Map<Integer, Integer> basePaymentAmounts = new HashMap<>();
	    Set<TrainingAttendanceDto> trainingDates = new TreeSet<>();
	    Map<Integer, Boolean> paidMap = new HashMap<>();

	    for (var r : records) {
	        int studentId = r.get("student_id", Integer.class);
	        String firstname = r.get(Student.STUDENT.FIRSTNAME);
	        String lastname = r.get(Student.STUDENT.LASTNAME);
	        String paymentType = r.get(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE);
	        int basePaymentAmount = Optional.ofNullable(r.get(StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT)).orElse(0);
	        int credit = Optional.ofNullable(r.get(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT)).orElse(0);
	        String studentType = r.get(Student.STUDENT.STUDENT_TYPE);
	        LocalDate date = r.get("training_date", LocalDate.class);
	        Boolean present = r.get(Attendance.ATTENDANCE.PRESENT);
	        int attendanceId = r.get("attendance_id", Integer.class);
	        int trainingId = r.get("training_id", Integer.class);
	        
	        boolean paid;
	        if (AppConstants.paymentType_monthly.equals(paymentType)) {
	            String monthYear = String.format("%02d/%d", month, year);
	            Integer count = dsl.selectCount()
	                .from(CreditTransaction.CREDIT_TRANSACTION)
	                .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId))
	                .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(martialArtId))
	                .and(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION.eq(monthYear))
	                .fetchOne(0, Integer.class);
	            paid = (count != null && count > 0);
	        } else if (AppConstants.paymentType_credit.equals(paymentType)) {
	            paid = credit > 0;
	        } else if (AppConstants.paymentType_yearly.equals(paymentType)) {
	            Integer count = dsl.selectCount()
	                .from(CreditTransaction.CREDIT_TRANSACTION)
	                .where(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID.eq(studentId))
	                .and(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID.eq(martialArtId))
	                .and(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION.eq(String.valueOf(year)))
	                .fetchOne(0, Integer.class);
	            paid = (count == 1);
	        } else {
	            paid = true; // NO_PAYMENT
	        }

	        trainingDates.add(new TrainingAttendanceDto(trainingId, date));
	        studentIds.putIfAbsent(studentId, studentId);
	        firstnames.putIfAbsent(studentId, firstname);
	        lastnames.putIfAbsent(studentId, lastname);
	        studentTypes.putIfAbsent(studentId, studentType);
	        credits.putIfAbsent(studentId, credit);
	        paymentTypes.putIfAbsent(studentId, paymentType);
	        basePaymentAmounts.putIfAbsent(studentId, basePaymentAmount);
	        paidMap.putIfAbsent(studentId, paid);

	        attendanceMap
	            .computeIfAbsent(studentId, id -> new LinkedHashMap<>())
	            .put(date, new AttendanceItemDto(attendanceId, present));
	    }

	    List<StudentAttendanceDto> studentDtos = attendanceMap.entrySet()
	        .stream()
	        .map(entry -> new StudentAttendanceDto(
	                studentIds.get(entry.getKey()),
	                firstnames.get(entry.getKey()),
	                lastnames.get(entry.getKey()),
	                credits.get(entry.getKey()),
	                studentTypes.get(entry.getKey()),
	                paymentTypes.get(entry.getKey()),
	                basePaymentAmounts.get(entry.getKey()),
	                paidMap.get(entry.getKey()),
	                entry.getValue()
	        ))
	        .toList();

	    return new AttendanceDto(
	        new ArrayList<>(trainingDates),
	        studentDtos,
	        color != null ? color : "brown"
	    );
	}
	
	@Transactional
	public void updateAttendance(int attendanceId, int trainingId, boolean present) {
	    // 1. Načítaj Attendance
	    var record = dsl.select(
	            Attendance.ATTENDANCE.PRESENT,
	            Attendance.ATTENDANCE.STUDENT_ID
	        )
	        .from(Attendance.ATTENDANCE)
	        .where(Attendance.ATTENDANCE.ID.eq(attendanceId))
	        .fetchOne();

	    if (record == null) 
	    	throw new IllegalArgumentException("Attendance not found");

	    boolean oldPresent = record.get(Attendance.ATTENDANCE.PRESENT);
	    int studentId = record.get(Attendance.ATTENDANCE.STUDENT_ID);

	    // 2. Načítaj martial_art_id z tréningu
	    var trainingRecord = dsl.select(Training.TRAINING.MARTIAL_ART_ID)
	        .from(Training.TRAINING)
	        .where(Training.TRAINING.ID.eq(trainingId))
	        .fetchOne();

	    if (trainingRecord == null)
	        throw new IllegalArgumentException("Training not found");

	    int martialArtId = trainingRecord.get(Training.TRAINING.MARTIAL_ART_ID);

	    // 3. Načítaj student_martial_art
	    var sma = dsl.select(
	    		StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE,
	    		StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT,
	    		StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT)
	        .from(StudentMartialArt.STUDENT_MARTIAL_ART)
	        .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
	        .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
	        .fetchOne();

	    if (sma == null) 
	    	throw new IllegalArgumentException("StudentMartialArt not found");

	    String paymentType = sma.get(StudentMartialArt.STUDENT_MARTIAL_ART.PAYMENT_TYPE);
	    int credit = Optional.ofNullable(sma.get(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT)).orElse(0);
	    int basePaymentAmount = Optional.ofNullable(sma.get(StudentMartialArt.STUDENT_MARTIAL_ART.BASE_PAYMENT_AMOUNT)).orElse(0);

	    // 4. Logika odpočtu/pripísania kreditu
	    if (AppConstants.paymentType_credit.equals(paymentType)) {
	        int delta = 0;
	        if (!oldPresent && present) 
	        	delta = -1;
	        else if (oldPresent && !present) 
	        	delta = +1;

	        if (delta != 0) {
	            dsl.update(StudentMartialArt.STUDENT_MARTIAL_ART)
	                .set(StudentMartialArt.STUDENT_MARTIAL_ART.CREDIT, credit + delta)
	                .where(StudentMartialArt.STUDENT_MARTIAL_ART.STUDENT_ID.eq(studentId))
	                .and(StudentMartialArt.STUDENT_MARTIAL_ART.MARTIAL_ART_ID.eq(martialArtId))
	                .execute();

	            dsl.insertInto(CreditTransaction.CREDIT_TRANSACTION)
	                .set(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID, studentId)
	                .set(CreditTransaction.CREDIT_TRANSACTION.MARTIAL_ART_ID, martialArtId)
	                .set(CreditTransaction.CREDIT_TRANSACTION.AMOUNT, delta * basePaymentAmount)
	                .set(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, delta > 0 ? "Vrátenie kreditu" : "Odpočet kreditu")
	                .set(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE, AppConstants.paymentType_credit)
	                .set(CreditTransaction.CREDIT_TRANSACTION.TRAINING_ID, trainingId)
	                .execute();
	        }
	    }

	    // 5. Aktualizuj Attendance
	    dsl.update(Attendance.ATTENDANCE)
	        .set(Attendance.ATTENDANCE.PRESENT, present)
	        .where(Attendance.ATTENDANCE.ID.eq(attendanceId))
	        .execute();
	}
}