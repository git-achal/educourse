package com.edu.educourse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * ============================================================
 *  CorsConfig - Cross-Origin Resource Sharing Configuration
 * ============================================================
 *
 *  CORS is a browser security mechanism that blocks JavaScript
 *  running on one origin (e.g., file:// or localhost:5500)
 *  from making requests to a different origin (localhost:8080).
 *
 *  Without this config, the browser will block all fetch() calls
 *  from the frontend HTML files to our Spring Boot API.
 *
 *  This config allows:
 *    - Any origin (*)       - so the HTML files work from any location
 *    - Any HTTP method      - GET, POST, DELETE, etc.
 *    - Any header           - including Authorization: Bearer <token>
 *    - Credentials          - so Authorization header is sent
 *
 *  In production: replace allowedOrigins("*") with your actual
 *  frontend domain e.g. "https://educourse.com"
 * ============================================================
 */
@Configuration
public class CorsConfig {

    /**
     * Registers a global CORS filter that applies to all requests
     * before they reach the Spring Security filter chain.
     *
     * This is registered as a CorsFilter (not WebMvcConfigurer)
     * so it works correctly with Spring Security.
     *
     * @return CorsFilter bean with permissive development settings
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow requests from any origin (frontend can be on any port or file://)
        config.addAllowedOriginPattern("*");

        // Allow all HTTP methods: GET, POST, PUT, DELETE, OPTIONS, etc.
        config.addAllowedMethod("*");

        // Allow all request headers, including Authorization (for JWT)
        config.addAllowedHeader("*");

        // Allow credentials (required for Authorization header to be sent)
        config.setAllowCredentials(true);

        // Apply this CORS config to all URL paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
