package com.alinbudesabc.authenticator.persistance.entities;


import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "usrloginfo")
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private long id;

    @Column(nullable = false)
    private String username;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "is_account_non_expired")
    private boolean isAccountNonExpired;

    @Column(name = "is_account_non_locked")
    private boolean isAccountNonLocked;

    @Column(name = "is_credentials_non_expired")
    private boolean isCredentialsNonExpired;

    @Column(name = "is_enabled")
    private boolean isEnabled;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;


    @Enumerated(EnumType.STRING)
    @Column(name = "auth_strategy")
    private AuthTypeEnum authStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "two_factor_message_strategy")
    private MessagingStrategyEnum twoFactorMessageStrategy;

    @JsonIgnore
    @Column(name = "mfa_secret")
    private String mfaSecret;


    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Transient
    public Set<RoleEnum> getRoles() {
        return userRoles.stream()
                .map(UserRoleEntity::getRole)
                .map(RoleEntity::getRoleName)
                .collect(Collectors.toSet());
    }

}
