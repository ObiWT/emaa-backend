package sk.emaa.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import sk.emaa.dto.ChangeAddressDto;
import sk.emaa.dto.ChangePasswordDto;
import sk.emaa.dto.ChangePaymentsDto;
import sk.emaa.dto.MartialArtDto;
import sk.emaa.dto.SchoolDto;
import sk.emaa.security.JwtProvider;
import sk.emaa.service.SchoolService;
import sk.emaa.service.SettingsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

	private final SettingsService settingsService;
	private final SchoolService schoolService;
	private final JwtProvider jwtProvider;

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto request,
			@RequestHeader("Authorization") String authHeader) {
		// validácia vstupu
		if (request.newPassword() == null || request.newPassword().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Heslo nemôže byť prázdne"));
		}

		try {
			// získanie tokenu z headeru "Bearer <token>"
			String token = authHeader.substring(7);

			// dekódovanie tokenu cez JwtTokenProvider
			Claims claims = jwtProvider.getClaims(token);

			String username = claims.getSubject(); // toto je "sub"
			// voliteľne môžeš získať aj role alebo schoolId
			// String role = (String) claims.get("role");
			// Integer schoolId = (Integer) claims.get("schoolI/martial-arts/{schoolId}d");

			settingsService.changePassword(username, request.newPassword());

			return ResponseEntity.ok(Map.of("success", true, "message", "Heslo zmenené"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "message", "Neplatný token alebo chyba pri zmene hesla"));
		}
	}

	@PatchMapping("/change-payments")
	public ResponseEntity<?> changePayments(@RequestBody ChangePaymentsDto request,
			@RequestHeader("Authorization") String authHeader) {
		settingsService.changePayments(request);
		return ResponseEntity.ok(Map.of("success", true, "message", "Platby zmenené"));
	}

	@PatchMapping("/change-address")
	public ResponseEntity<?> changeAddress(@RequestBody ChangeAddressDto request,
			@RequestHeader("Authorization") String authHeader) {
		Integer schoolId = jwtProvider.getSchoolIdFromAuthHeader(authHeader);

		settingsService.changeAddress(schoolId, request);

		return ResponseEntity.ok(Map.of("success", true, "message", "Adresa zmenená"));
	}

	@GetMapping("/school")
	public SchoolDto getSchool(@RequestHeader("Authorization") String authHeader) {
		Integer schoolId = jwtProvider.getSchoolIdFromAuthHeader(authHeader);

		return schoolService.getSchool(schoolId);
	}

	@GetMapping("/martial-arts")
	public List<MartialArtDto> getMartialArts(@RequestHeader("Authorization") String authHeader) {
		Integer schoolId = jwtProvider.getSchoolIdFromAuthHeader(authHeader);
		return settingsService.getMartialArts(schoolId);
	}

	@PatchMapping("/martial-art/{martialArtId}/active")
	public ResponseEntity<?> updateMartialArtActive(@PathVariable int martialArtId, @RequestParam boolean active) {
		settingsService.updateMartialArtActive(martialArtId, active);
		return ResponseEntity.ok(Map.of("success", true));
	}

}
