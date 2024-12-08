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

    private final OAuthLoginService oAuthLoginService;

    @Autowired
    public SecurityConfig(OAuthLoginService oAuthLoginService) {
        this.oAuthLoginService = oAuthLoginService;
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
                            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                            oAuthLoginService.handleGithubOAuthLogin(oAuth2User);
                        })
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("/login?error");
                        })
                );

        return http.build();
    }


}
