package com.ziminpro.ums.dtos;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "roles")
public class Roles {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    UUID id;

    String name;
    String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public Roles(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
