package com.ziminpro.ums.dao;

import java.time.Instant;
import java.util.*;

import com.ziminpro.ums.dtos.Constants;
import com.ziminpro.ums.dtos.LastSession;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUmsRepository implements UmsRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<UUID, User> findAllUsers() {
        Map<UUID, User> users = new HashMap<>();

        List<Object> oUsers = jdbcTemplate.query(Constants.GET_ALL_USERS,
                (rs, rowNum) -> new User(
                        DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")),
                        rs.getString("users.name"),
                        rs.getString("users.email"),
                        rs.getString("users.password"),
                        rs.getInt("users.created"),
                        new LastSession(rs.getInt("last_visit.in"), rs.getInt("last_visit.out"))
                ));

        for (Object oUser : oUsers) {
            User user = (User) oUser;

            // Fetch roles for each user
            Set<Roles> roles = findRolesByUserId(user.getId());
            user.setRoles(roles);

            users.put(user.getId(), user);
        }
        return users;
    }

    @Override
    public User findUserByID(UUID userId) {
        User user = jdbcTemplate.queryForObject(Constants.GET_USER_BY_ID_FULL, (rs, rowNum) -> new User(
                DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")),
                rs.getString("users.name"),
                rs.getString("users.email"),
                rs.getString("users.password"),
                rs.getInt("users.created"),
                new LastSession(rs.getInt("last_visit.in"), rs.getInt("last_visit.out"))
        ), userId.toString());

        if (user != null) {
            Set<Roles> roles = findRolesByUserId(userId);
            user.setRoles(roles);
        }

        return user;
    }

    private Set<Roles> findRolesByUserId(UUID userId) {
        List<Roles> roles = jdbcTemplate.query(Constants.GET_ROLES_BY_USER_ID,
                (rs, rowNum) -> new Roles(
                        DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                        rs.getString("roles.name"),
                        rs.getString("roles.description")
                ),
                userId.toString());

        return new HashSet<>(roles);
    }

    @Override
    public UUID createUser(User user) {
        long timestamp = Instant.now().getEpochSecond();
        UUID userId = UUID.randomUUID();

        try {
            // Insert user
            jdbcTemplate.update(Constants.CREATE_USER, userId.toString(), user.getName(), user.getEmail(),
                    user.getPassword(), timestamp, null);

            // Insert roles
            for (Roles role : user.getRoles()) {
                jdbcTemplate.update(Constants.ASSIGN_ROLE, userId.toString(), role.getId().toString());
            }
        } catch (Exception e) {
            return null;
        }

        return userId;
    }

    @Override
    public int deleteUser(UUID userId) {
        return jdbcTemplate.update(Constants.DELETE_USER, userId.toString());
    }

    @Override
    public Map<String, Roles> findAllRoles() {
        Map<String, Roles> roles = new HashMap<>();
        jdbcTemplate.query(Constants.GET_ALL_ROLES, rs -> {
            Roles role = new Roles(
                    DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                    rs.getString("roles.name"),
                    rs.getString("roles.description")
            );
            roles.put(rs.getString("roles.name"), role);
        });
        return roles;
    }
}
