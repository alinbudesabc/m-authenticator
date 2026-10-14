package com.alinbudesabc.authenticator.persistance.entities;

import com.alinbudesabc.authenticator.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    private RoleEnum roleName;

    @OneToMany(mappedBy = "role", orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    public RoleEntity(RoleEnum role) {
        this.roleName = role;
    }
}
