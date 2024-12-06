package com.ziminpro.ums;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.UUID;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/login**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                    .successHandler((request, response, authentication) -> {
                        response.sendRedirect("/");
                    })
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("/login?error");
                    })
            );

        return http.build();
    }

    public String generateUserToken(String githubUserId) {
        String token = UUID.randomUUID().toString();
        return token;
    }
}
