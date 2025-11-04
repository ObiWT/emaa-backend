package sk.emaa.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.LoginRequest;
import sk.emaa.dto.StudentDto;
import sk.emaa.service.StudentService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {
	
	private final StudentService studentService;
	
	@GetMapping("/students/{schoolId}")
	public List<StudentDto> loadStudents(@PathVariable int schoolId) {
		return studentService.loadStudents(schoolId);
	}
	
	@PostMapping("/student")
	void createStudent(@RequestBody StudentDto student) {
		studentService.createStudent(student);
	}
	
	@PutMapping("/student")
	void updateStudent() {
		
	}
	
	@GetMapping("/student")
	void getStudent() {
		
	}

}
