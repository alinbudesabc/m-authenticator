package com.alinbudesabc.authenticator.persistance.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
public class UserRoleEntity {

//    public UserRoleEntity(UserEntity user, RoleEntity role) {
//        this.user = user;
//        this.role = role;
//        this.id.setUserId(user.getId());
//        this.id.setRoleId(role.getId());
//    }

    public UserRoleEntity(UserEntity user, RoleEntity role) {
        this.user = user;
        this.role = role;
        //this.id = new UserRoleKey(user.getId(), role.getId());
    }

    @EmbeddedId
    private UserRoleKey id = new UserRoleKey();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private RoleEntity role;
}
