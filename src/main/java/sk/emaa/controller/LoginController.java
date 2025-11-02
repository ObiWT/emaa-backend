package sk.emaa.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sk.emaa.dto.LoginRequest;
import sk.emaa.service.AuthService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {
	
	private final AuthService authService;

	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		
		try {
            String token = authService.login(request.username(), request.password());

            // vraciame JSON s tokenom
            return ResponseEntity.ok(Map.of("token", token));

        } catch (RuntimeException e) {
            // ak niečo nevyjde (zlé heslo, neexistujúci user)
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
	
	
}
