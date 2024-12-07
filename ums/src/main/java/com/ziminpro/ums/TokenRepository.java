package com.ziminpro.ums;

import com.ziminpro.ums.dtos.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<Token, String> {
    Token findByToken(String token);

    @Query(value = "SELECT * FROM ums.tokens t WHERE t.github_user_id = :githubUserId ORDER BY t.expiration_time DESC LIMIT 1", nativeQuery = true)
    Token findByGithubUserId(@Param("githubUserId") String githubUserId);
}
