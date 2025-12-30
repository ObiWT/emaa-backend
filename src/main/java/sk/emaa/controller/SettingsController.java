package sk.emaa.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import sk.emaa.dto.ChangePasswordDto;
import sk.emaa.security.JwtProvider;
import sk.emaa.service.SettingsService;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {
	
	private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
	
	private final SettingsService settingsService;
	private final JwtProvider jwtProvider;

	@PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto request, @RequestHeader("Authorization") String authHeader) {
		logger.debug("AUTH HEADER: " + authHeader);
		System.out.println("AUTH HEADER: " + authHeader);
        // validácia vstupu
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Heslo nemôže byť prázdne"));
        }

        try {
            // získanie tokenu z headeru "Bearer <token>"
            String token = authHeader.substring(7);

            // dekódovanie tokenu cez JwtTokenProvider
            Claims claims = jwtProvider.getClaims(token);

            String username = claims.getSubject();  // toto je "sub"
            // voliteľne môžeš získať aj role alebo schoolId
            // String role = (String) claims.get("role");
            // Integer schoolId = (Integer) claims.get("schoolId");

            settingsService.changePassword(username, request.newPassword());

            return ResponseEntity.ok(Map.of("success", true, "message", "Heslo zmenené"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Neplatný token alebo chyba pri zmene hesla"));
        }
    }
	
}
