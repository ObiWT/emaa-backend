package sk.emaa.config;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityFilterChain.class);

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		logger.info("SecurityConfig: Vytváram SecurityFilterChain");
		
        http
            .cors(withDefaults()) // aktivuje CORS podľa nižšieho beanu
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            );
        
        logger.info("SecurityConfig: SecurityFilterChain hotový");

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
	    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
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
        return new BCryptPasswordEncoder();
    }
    
}
