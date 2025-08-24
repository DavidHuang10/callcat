package com.callcat.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

// Test-specific security configuration for unit and integration tests
// Disables certain security features to make testing easier while maintaining core functionality
// Used in @WebMvcTest to provide controlled security environment
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    // Configures simplified security filter chain for testing
    // Allows testing of authentication endpoints while protecting sensitive ones
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/webhooks/**").permitAll()  // Webhooks are public endpoints
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/calls/**").authenticated()
                        .requestMatchers("/api/live_transcripts/**").authenticated()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        
        return http.build();
    }
}