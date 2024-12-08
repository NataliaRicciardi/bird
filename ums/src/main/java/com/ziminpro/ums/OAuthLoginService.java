package com.ziminpro.ums;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.Token;
import com.ziminpro.ums.dtos.User;
import com.ziminpro.ums.dtos.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OAuthLoginService {
    private final UmsRepository umsRepository;
    private final TokenRepository tokenRepository;

    @Autowired
    public OAuthLoginService(UmsRepository umsRepository, TokenRepository tokenRepository) {
        this.umsRepository = umsRepository;
        this.tokenRepository = tokenRepository;
    }

    public void handleGithubOAuthLogin(OAuth2User oauth2User) {
        String githubUserId = getAttributeAsString(oauth2User, "id", "Unknown");
        String name = getAttributeAsString(oauth2User, "name", "Unknown");
        String email = getAttributeAsString(oauth2User, "email", "No email provided");

        User user = umsRepository.findAllUsers()
                .values()
                .stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            user = new User();
            UUID userId = UUID.randomUUID();
            user.setId(userId);
            user.setName(name);
            user.setEmail(email);
            user.setPassword(""); // No password for OAuth users
            user.setCreated((int) (System.currentTimeMillis() / 1000));

            umsRepository.createUser(user);

            UUID subscriberRoleId = umsRepository.findAllRoles()
                    .values()
                    .stream()
                    .filter(role -> "subscriber".equalsIgnoreCase(role.getName()))
                    .map(Roles::getId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Subscriber role not found"));

            umsRepository.assignRole(user.getId(), subscriberRoleId);
        }

        Token tokenEntity = new Token();
        tokenEntity.setToken(UUID.randomUUID().toString());
        tokenEntity.setGithubUserId(githubUserId);
        tokenEntity.setName(name);
        tokenEntity.setEmail(email);
        tokenEntity.setExpirationTime(LocalDateTime.now().plusMinutes(15));
        tokenEntity.setUser(user);

        tokenRepository.save(tokenEntity);
    }

    private String getAttributeAsString(OAuth2User user, String attributeName, String defaultValue) {
        Object attribute = user.getAttribute(attributeName);
        return attribute == null ? defaultValue : attribute.toString();
    }
}
