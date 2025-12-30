package sk.emaa.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import sk.emaa.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		logger.info("SecurityConfig: Vytváram SecurityFilterChain");
		
		http
        .cors(withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/login"
            ).permitAll()
            .anyRequest().authenticated()
        )
        // 👇 KRITICKÉ
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class
        );

		return http.build();
    }

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

	    logger.info("SecurityConfig: Vytváram CorsConfigurationSource");

	    CorsConfiguration configuration = new CorsConfiguration();

	    configuration.setAllowedOriginPatterns(Arrays.asList(
    		"https://emaa-frontend.onrender.com",
    	    "http://localhost:*",
    	    "http://192.168.*:*"
	    ));
	    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
	    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
	    configuration.setAllowCredentials(true);
	    configuration.setMaxAge(3600L);

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);

	    logger.info("SecurityConfig: CorsConfigurationSource hotový");

	    return source;
	}
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("SecurityConfig: Vytváram PasswordEncoder bean");
        return new BCryptPasswordEncoder(12);
    }
    
}
