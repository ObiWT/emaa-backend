package sk.emaa.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    //@Value("${jwt.secret}")
    //private String secretKey; // musí byť dostatočne dlhé, min 256 bitov pre HS256

    @Value("${jwt.expiration}")
    private long validityInMs; // napr. 3600000 = 1 hodina

    private Key getSigningKey() {
        // return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    /**
     * Vytvorenie JWT tokenu pre daného používateľa a jeho rolu
     */
    public String createToken(String username, String role) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey()) // nový spôsob bez deprecated metódy
                .compact();
    }

    /**
     * Overenie platnosti tokenu
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Získanie username z tokenu
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Získanie role z tokenu
     */
    public String getRole(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }
}
