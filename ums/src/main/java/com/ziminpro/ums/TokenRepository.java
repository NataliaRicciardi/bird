package com.ziminpro.ums;

import com.ziminpro.ums.dtos.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, String> {
    Token findByToken(String token);

    @Query(value = "SELECT * FROM ums.tokens t WHERE t.github_user_id = :githubUserId ORDER BY t.expiration_time DESC LIMIT 1", nativeQuery = true)
    Token findByGithubUserId(@Param("githubUserId") String githubUserId);

    @Query(value = """
        SELECT r.name 
        FROM ums.roles r
        JOIN ums.users_has_roles ur ON r.id = ur.roles_id
        JOIN ums.users u ON ur.users_id = u.id
        JOIN ums.tokens t ON u.id = t.user_id
        WHERE t.token = :token
    """, nativeQuery = true)
    List<String> findRolesByToken(@Param("token") String token);
}
