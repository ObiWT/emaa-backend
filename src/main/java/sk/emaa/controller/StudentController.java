package sk.emaa.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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
	public void createStudent(@RequestBody StudentDto student) {
		studentService.createStudent(student);
	}
	
	@PutMapping("/student")
	public void updateStudent(@RequestBody StudentDto student) {
	    studentService.updateStudent(student);
	}
	
	@GetMapping("/student/{id}")
	public StudentDto getStudent(@PathVariable int id) {
		return studentService.getStudent(id);
	}

}
