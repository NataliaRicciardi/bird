package com.ziminpro.ums;

import com.ziminpro.ums.dtos.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, String> {
    Token findByToken(String token);
}
