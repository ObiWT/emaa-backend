package sk.emaa.config;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@Configuration
public class GlobalCorsConfig {
/*	
	private static final Logger logger = LoggerFactory.getLogger(GlobalCorsConfig.class);

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // 💥 spustí sa pred všetkým
    public CorsFilter corsFilter() {
    	
    	logger.info("GlobalCorsConfig: CorsFilter bean inicializovaný");
    	
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(
            "https://emaa-frontend.onrender.com",
            "http://localhost:*",
            "http://192.168.*:*"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
            	logger.info("GlobalCorsConfig: CorsFilter prechádza request " + request.getMethod() + " " + request.getRequestURI());
                super.doFilterInternal(request, response, filterChain);
            }
        };
    }
    */
}
