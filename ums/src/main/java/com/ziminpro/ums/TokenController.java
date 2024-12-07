package com.ziminpro.ums;

import com.ziminpro.ums.dtos.Token;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ums")
public class TokenController {
    private final TokenRepository tokenRepository;

    public TokenController(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
        Token token = tokenRepository.findByToken(request.getToken());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        List<String> roles = tokenRepository.findRolesByToken(request.getToken());
        if (roles.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No roles assigned to the token");
        }

        return ResponseEntity.ok(new TokenValidationResponse(token.getGithubUserId(), roles));
    }


    public class TokenValidationRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    @Getter
    @Setter
    public class TokenValidationResponse {
        private String githubUserId;
        private List<String> roles;

        public TokenValidationResponse(String githubUserId, List<String> roles) {
            this.githubUserId = githubUserId;
            this.roles = roles;
        }
    }
}


