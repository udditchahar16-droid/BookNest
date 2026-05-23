package com.booknest.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

/**
 * Security configuration for booknest-web.
 *
 * Authentication is NOT handled by Spring Security here — the auth-service
 * issues JWT tokens and the controllers verify them manually via session.
 * Spring Security is kept in the classpath only for Thymeleaf
 * sec:authorize tags and CSRF protection.
 *
 * All routes are permitted at the framework level; the controllers themselves
 * redirect unauthenticated users to /auth/login when required.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Permit every URL — controllers handle their own auth checks
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Disable form login (we have our own /auth/login page)
            .formLogin(form -> form.disable())
            // Keep CSRF enabled for form submissions (Thymeleaf adds the token automatically)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // In case any REST endpoints are added later
            );
        return http.build();
    }

    /**
     * Shared RestTemplate bean used by all MVC controllers to call microservices.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
