package com.ziminpro.ums.dtos;

import java.util.*;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    UUID id;
    String name;
    String email;
    String password;
    int created;
    @ManyToMany
    @JoinTable(
            name = "users_has_roles",
            joinColumns = @JoinColumn(name = "users_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id")
    )
    private Set<Roles> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Token> tokens;

    @OneToOne
    @JoinColumn(name = "last_visit_id")
    private LastSession lastSession;

    public User(UUID id, String name, String email, String password, int created, LastSession lastSession) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.created = created;
        this.lastSession = lastSession;
    }

    public void addRole(Roles role) {
        this.roles.add(role);
    }
}