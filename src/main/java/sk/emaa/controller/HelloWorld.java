package sk.emaa.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class HelloWorld {

	@GetMapping("/hello")
	public String hello(HttpServletRequest request) {
	    String username = (String) request.getAttribute("username");
	    return "Hello " + username + " 👋";
	}
	
}
