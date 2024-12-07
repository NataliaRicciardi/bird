package com.ziminpro.ums;

import com.ziminpro.ums.dtos.Token;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Configuration
public class SecurityConfig {

    private final TokenRepository tokenRepository;

    @Autowired
    public SecurityConfig(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/login**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                    .successHandler((request, response, authentication) -> {

                        handleLoginSuccess((OAuth2AuthenticationToken) authentication);

                        response.sendRedirect("/");

                    })
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("/login?error");
                    })
            );

        return http.build();
    }

    public void handleLoginSuccess(OAuth2AuthenticationToken authentication) {
        OAuth2User user = authentication.getPrincipal();

        Map<String, Object> attributes = user.getAttributes();

        attributes.forEach((key, value) -> {
            if (value != null) {
                System.out.println("Attribute Key: " + key + ", Value: " + value + ", Type: " + value.getClass());
            } else {
                System.out.println("Attribute Key: " + key + ", Value: null");
            }
        });

        Object githubUserIdObject = user.getAttribute("id");
        String githubUserId = (githubUserIdObject instanceof Integer)
                ? String.valueOf(githubUserIdObject)
                : githubUserIdObject != null ? githubUserIdObject.toString() : "Unknown";

        String name = getAttributeAsString(user, "name", "Unknown");
        String email = getAttributeAsString(user, "email", "No email provided");

        String token = generateUserToken(githubUserId);

        Token tokenEntity = new Token();
        tokenEntity.setToken(token);
        tokenEntity.setGithubUserId(githubUserId);
        tokenEntity.setName(name);
        tokenEntity.setEmail(email);
        tokenEntity.setExpirationTime(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(tokenEntity);
    }

    public String generateUserToken(String githubUserId) {
        String token = UUID.randomUUID().toString();
        return token;
    }

    private String getAttributeAsString(OAuth2User user, String attributeName, String defaultValue) {
        Object attribute = user.getAttribute(attributeName);
        if (attribute == null) {
            return defaultValue;
        }
        else if (attribute instanceof String) {
            return (String) attribute;
        }
        else {
            return attribute.toString();
        }
    }
}
