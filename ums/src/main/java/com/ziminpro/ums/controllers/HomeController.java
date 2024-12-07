package com.ziminpro.ums.controllers;

import com.ziminpro.ums.TokenRepository;
import com.ziminpro.ums.dtos.Token;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final TokenRepository tokenRepository;

    public HomeController(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/")
    public String home() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;

            Object githubUserIdObject = oAuth2User.getAttribute("id");
            String githubUserId = (githubUserIdObject instanceof Integer)
                    ? String.valueOf(githubUserIdObject)
                    : githubUserIdObject != null ? githubUserIdObject.toString() : "Unknown";

            Token token = tokenRepository.findByGithubUserId(githubUserId);

            if (token != null) {
                return String.format(
                        "Welcome, %s!<br>Email: %s<br>Your Token: %s",
                        token.getName(),
                        token.getEmail(),
                        token.getToken()
                );
            } else {
                return "Welcome! We could not retrieve your token.";
            }
        }

        return "Welcome to the application!";
    }

}
