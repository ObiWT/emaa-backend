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

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.AttendanceDto;
import sk.emaa.dto.StudentAttendanceDto;
import sk.emaa.dto.TrainingDto;
import sk.emaa.model.entity.tables.Attendance;
import sk.emaa.model.entity.tables.Student;
import sk.emaa.model.entity.tables.Training;
import sk.emaa.model.entity.tables.records.TrainingRecord;

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

	    Map<Integer, Map<LocalDate, Boolean>> attendanceMap = new LinkedHashMap<>();
	    Map<Integer, String> firstnames = new HashMap<>();
	    Map<Integer, String> lastnames = new HashMap<>();
	    Set<LocalDate> trainingDates = new TreeSet<>();

	    for (var r : records) {
	        int studentId = r.get("student_id", Integer.class);
	        String firstname = r.get(Student.STUDENT.FIRSTNAME);
	        String lastname = r.get(Student.STUDENT.LASTNAME);
	        LocalDate date = r.get("training_date", LocalDate.class);
	        Boolean present = r.get(Attendance.ATTENDANCE.PRESENT);

	        trainingDates.add(date);

	        firstnames.putIfAbsent(studentId, firstname);
	        lastnames.putIfAbsent(studentId, lastname);

	        attendanceMap
	            .computeIfAbsent(studentId, id -> new LinkedHashMap<>())
	            .put(date, present);
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

}
