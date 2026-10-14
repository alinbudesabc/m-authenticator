package com.alinbudesabc.authenticator.forgotPassword;

import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reset_password_token")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private LocalDateTime expirationTime;


    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public PasswordResetTokenEntity(String token, UserEntity user) {
        this.token = token;
        this.user = user;
    }
}
