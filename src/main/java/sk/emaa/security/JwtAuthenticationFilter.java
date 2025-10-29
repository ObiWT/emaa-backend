package sk.emaa.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sk.emaa.util.JwtUtil;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 🔹 ak v hlavičke nie je token, pokračuj normálne
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // odstráni "Bearer "
        try {
            // 🔹 overenie tokenu
            Claims claims = JwtUtil.validateToken(token);
            String username = claims.getSubject();

            // tu by sa dala nastaviť autentifikácia do SecurityContextHolder
            // napr. SecurityContextHolder.getContext().setAuthentication(...)

            request.setAttribute("username", username); // pre jednoduché použitie

        } catch (Exception e) {
            // 🔹 token neplatný alebo expirovaný
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 🔹 ak je všetko OK, pokračuj ďalej
        filterChain.doFilter(request, response);
    }
	
}
