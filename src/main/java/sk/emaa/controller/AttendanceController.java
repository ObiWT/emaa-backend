package sk.emaa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.AttendanceDto;
import sk.emaa.dto.TrainingDto;
import sk.emaa.service.AttendanceService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

	private final AttendanceService attendanceService;

	@PostMapping("/training")
    public void addTraining(@RequestBody TrainingDto request) {
		attendanceService.addTraining(request);
    }
	
	@GetMapping("/attendance/{schoolId}/{month}/{year}")
    public ResponseEntity<AttendanceDto> getAttendance(@PathVariable int schoolId, @PathVariable int month, @PathVariable int year) {
		AttendanceDto dto = attendanceService.getAttendance(schoolId, month, year);
		return ResponseEntity.ok(dto);
    }
	
}
