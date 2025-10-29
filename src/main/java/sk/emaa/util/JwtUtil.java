package sk.emaa.util;

import java.util.Date;
import java.security.Key;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
	
	/*
	 * kľúč uložiť do application.properties alebo do databázy, aby tokeny vydané pri reštarte servera zostali platné.
	 * Inak sa pri reštarte servera vygeneruje nový SECRET_KEY a staré tokeny prestanú platiť.
	 */
	
	// 🔹 bezpečný kľúč (256-bit pre HS256)
	private static final Key SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
	
	public static String generateToken(String username) {
		long expirationMillis = 3600000; // 1 hodina
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis() + expirationMillis))
				.signWith(SECRET_KEY)
				.compact();
	}
	
	// 🔹 overenie tokenu
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(SECRET_KEY)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

}
