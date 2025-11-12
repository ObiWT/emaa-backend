package sk.emaa.service;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.AttendanceDto;
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
	
	public List<AttendanceDto> getAttendance(int schoolId, int month, int year) {
		// TODO implement
		return null;
	}
}
