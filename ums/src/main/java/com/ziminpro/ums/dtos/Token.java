package com.ziminpro.ums.dtos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tokens")
public class Token {
    @Id
    private String token;
    private String githubUserId;
    private String name;
    private String email;
    private LocalDateTime expirationTime;
}
