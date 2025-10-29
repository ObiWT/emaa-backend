package sk.emaa.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sk.emaa.dto.LoginRequest;

@RestController
@RequestMapping("/api")
public class LoginController {

	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		String username = request.username();
		String password = request.password();
		System.out.println("USERNAME: " + username + " and PASSWORD: " + password);
		
		if ("admin".equals(username) && "admin".equals(password)) {
			String fakeToken = "abc123xyz456";
			Map<String, String> response = new HashMap<String, String>();
			response.put("token", fakeToken);
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
    }
	
	
}
