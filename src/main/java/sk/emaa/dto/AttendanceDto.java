package sk.emaa.dto;

import java.time.LocalDate;
import java.util.List;

public record AttendanceDto (List<LocalDate> trainings, List<StudentAttendanceDto> students) {
    
}
