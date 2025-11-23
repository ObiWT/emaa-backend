package sk.emaa.dto;

import java.time.LocalDate;
import java.util.Map;

public record StudentAttendanceDto (String firstname, String lastname, Map<LocalDate, Boolean> attendance) {
}
