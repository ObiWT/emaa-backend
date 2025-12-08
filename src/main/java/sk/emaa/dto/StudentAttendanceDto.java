package sk.emaa.dto;

import java.time.LocalDate;
import java.util.Map;

public record StudentAttendanceDto (
		int studentId,
		String firstname, 
		String lastname,
		int credit,
		String paymentType,
		boolean paid, 
		Map<LocalDate, AttendanceItemDto> attendance) {
}
