package com.ziminpro.ums.dao;

import java.nio.ByteBuffer;
import java.sql.SQLException;
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

        // Query to fetch all user details including roles
        List<User> userList = jdbcTemplate.query(Constants.GET_ALL_USERS, (rs) -> {
            Map<UUID, User> tempUsers = new HashMap<>();

            while (rs.next()) {
                UUID userId = DaoHelper.bytesArrayToUuid(rs.getBytes("users.id"));

                User user = tempUsers.computeIfAbsent(userId, id -> {
                    try {
                        return new User(
                                id,
                                rs.getString("users.name"),
                                rs.getString("users.email"),
                                rs.getString("users.password"),
                                rs.getInt("users.created"),
                                new LastSession(
                                        rs.getInt("last_visit.in"),
                                        rs.getInt("last_visit.out")
                                )
                        );
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                // Add roles to the user
                if (rs.getBytes("roles.id") != null) { // Ensure role exists
                    Roles role = new Roles(
                            DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                            rs.getString("roles.name"),
                            rs.getString("roles.description")
                    );
                    user.getRoles().add(role);
                }
            }

            return new ArrayList<>(tempUsers.values());
        });

        for (User user : userList) {
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

    public void assignRole(UUID userId, UUID roleId) {
        byte[] userIdBytes = asBytes(userId);
        byte[] roleIdBytes = asBytes(roleId);

    }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }
}
