package sk.emaa.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import sk.emaa.dto.TrainingDto;
import sk.emaa.model.entity.tables.Attendance;
import sk.emaa.model.entity.tables.CreditTransaction;
import sk.emaa.model.entity.tables.Student;
import sk.emaa.model.entity.tables.Training;
import sk.emaa.model.entity.tables.records.TrainingRecord;
import sk.emaa.util.AppConstants;

@Service
@RequiredArgsConstructor
public class AttendanceService {
	
	private final DSLContext dsl;

	public void addTraining(TrainingDto training) {
	    dsl.transaction(configuration -> {
	        DSLContext dsl = DSL.using(configuration);

	        // 1️. Vloženie nového tréningu a získanie ID
	        TrainingRecord trainingRecord = dsl.insertInto(Training.TRAINING)
	            .set(Training.TRAINING.SCHOOL_ID, training.schoolId())
	            .set(Training.TRAINING.DATE, training.date())
	            .returning(Training.TRAINING.ID)
	            .fetchOne();

	        if (trainingRecord == null) {
	            throw new IllegalStateException("Failed to create training record");
	        }

	        int trainingId = trainingRecord.getId();

	        // 2. Hromadné vloženie študentov do attendance cez INSERT ... SELECT
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
	                    .where(Student.STUDENT.SCHOOL_ID.eq(training.schoolId()))
	            )
	            .execute();
	    });
	}
	
	public AttendanceDto getAttendance(int schoolId, int month, int year) {

	    YearMonth yearMonth = YearMonth.of(year, month);
	    LocalDate start = yearMonth.atDay(1);
	    LocalDate end = yearMonth.atEndOfMonth();

	    var records = dsl.select(
	            Student.STUDENT.ID.as("student_id"),
	            Student.STUDENT.FIRSTNAME,
	            Student.STUDENT.LASTNAME,
	            Attendance.ATTENDANCE.ID.as("attendance_id"),
	            Training.TRAINING.ID.as("training_id"),
	            Training.TRAINING.DATE.as("training_date"),
	            Attendance.ATTENDANCE.PRESENT
	        )
	        .from(Student.STUDENT)
	        .join(Attendance.ATTENDANCE)
	            .on(Attendance.ATTENDANCE.STUDENT_ID.eq(Student.STUDENT.ID))
	        .join(Training.TRAINING)
	            .on(Training.TRAINING.ID.eq(Attendance.ATTENDANCE.TRAINING_ID))
	        .where(Student.STUDENT.SCHOOL_ID.eq(schoolId))
	        .and(Training.TRAINING.DATE.between(start, end))
	        .orderBy(Student.STUDENT.LASTNAME.asc(), Training.TRAINING.DATE.asc())
	        .fetch();

	    Map<Integer, Map<LocalDate, AttendanceItemDto>> attendanceMap = new LinkedHashMap<>();
	    Map<Integer, String> firstnames = new HashMap<>();
	    Map<Integer, String> lastnames = new HashMap<>();
	    Set<LocalDate> trainingDates = new TreeSet<>();

	    for (var r : records) {
	        int studentId = r.get("student_id", Integer.class);
	        String firstname = r.get(Student.STUDENT.FIRSTNAME);
	        String lastname = r.get(Student.STUDENT.LASTNAME);
	        LocalDate date = r.get("training_date", LocalDate.class);
	        Boolean present = r.get(Attendance.ATTENDANCE.PRESENT);
	        int attendanceId = r.get("attendance_id", Integer.class);

	        trainingDates.add(date);
	        firstnames.putIfAbsent(studentId, firstname);
	        lastnames.putIfAbsent(studentId, lastname);

	        attendanceMap
	            .computeIfAbsent(studentId, id -> new LinkedHashMap<>())
	            .put(date, new AttendanceItemDto(attendanceId, present));
	    }

	    // vytvorenie recordov
	    List<StudentAttendanceDto> studentDtos = attendanceMap.entrySet()
	        .stream()
	        .map(entry -> new StudentAttendanceDto(
	                firstnames.get(entry.getKey()),
	                lastnames.get(entry.getKey()),
	                entry.getValue()
	        ))
	        .toList();

	    return new AttendanceDto(
	        new ArrayList<>(trainingDates),
	        studentDtos
	    );
	}
	
	@Transactional
	public void updateAttendance(int attendanceId, boolean present) {
	    // 1. Načítaj Attendance + Student
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

	    // 2. Načítaj študenta
	    var student = dsl.select(Student.STUDENT.PAYMENT_TYPE, Student.STUDENT.CREDIT)
	                     .from(Student.STUDENT)
	                     .where(Student.STUDENT.ID.eq(studentId))
	                     .fetchOne();

	    if (student == null) 
	    	throw new IllegalArgumentException("Student not found");

	    String paymentType = student.get(Student.STUDENT.PAYMENT_TYPE);
	    int credit = student.get(Student.STUDENT.CREDIT);

	    // 3. Logika odpočtu/pripísania kreditu
	    if (AppConstants.paymentType_credit.equals(paymentType)) {
	        int delta = 0;
	        if (!oldPresent && present) 
	        	delta = -1;  // odpočítať kredit
	        else if (oldPresent && !present) 
	        	delta = +1;  // vrátiť kredit

	        if (delta != 0) {
	            dsl.update(Student.STUDENT)
	                .set(Student.STUDENT.CREDIT, credit + delta)
	                .where(Student.STUDENT.ID.eq(studentId))
	                .execute();

	            dsl.insertInto(CreditTransaction.CREDIT_TRANSACTION)
	                .set(CreditTransaction.CREDIT_TRANSACTION.STUDENT_ID, studentId)
	                .set(CreditTransaction.CREDIT_TRANSACTION.AMOUNT, delta * AppConstants.creditPayment) // vynasobim cenu jedneho treningu
	                .set(CreditTransaction.CREDIT_TRANSACTION.DESCRIPTION, delta > 0 ? "Vrátenie kreditu" : "Odpočet kreditu")
	                .set(CreditTransaction.CREDIT_TRANSACTION.PAYMENT_TYPE, AppConstants.paymentType_credit)
	                .execute();
	        }
	    }

	    // 4. Aktualizuj Attendance
	    dsl.update(Attendance.ATTENDANCE)
	        .set(Attendance.ATTENDANCE.PRESENT, present)
	        .where(Attendance.ATTENDANCE.ID.eq(attendanceId))
	        .execute();
	}

}
