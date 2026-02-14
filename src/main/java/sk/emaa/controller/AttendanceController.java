package sk.emaa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<String> addTraining(@RequestBody TrainingDto request) {
		try {
			attendanceService.addTraining(request);
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
            // duplicitný tréning alebo iný biznis problém
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
	
	@DeleteMapping("/training/{trainingId}")
    public ResponseEntity<String> deleteTraining(@PathVariable int trainingId) {
		try {
			attendanceService.deleteTraining(trainingId);
			return ResponseEntity.ok().build();
		} catch (IllegalStateException e) {
            // duplicitný tréning alebo iný biznis problém
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
	
	@GetMapping("/attendance/{schoolId}/{month}/{year}/{martialArtId}")
    public ResponseEntity<AttendanceDto> getAttendance(@PathVariable int schoolId, @PathVariable int month, @PathVariable int year, @PathVariable int martialArtId) {
		AttendanceDto dto = attendanceService.getAttendance(schoolId, month, year, martialArtId);
		return ResponseEntity.ok(dto);
    }
	
	@PatchMapping("/attendance/{attendanceId}/{trainingId}")
	public ResponseEntity<?> updateAttendance(@PathVariable int attendanceId, @PathVariable int trainingId, @RequestParam boolean present) {
		attendanceService.updateAttendance(attendanceId, trainingId, present);
		return ResponseEntity.ok().build();
	}
	
}
