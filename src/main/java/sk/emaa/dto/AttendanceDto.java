package sk.emaa.dto;

import java.util.List;

public record AttendanceDto (List<TrainingAttendanceDto> trainings, List<StudentAttendanceDto> students) {
    
}
