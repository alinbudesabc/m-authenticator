package com.alinbudesabc.authenticator.forgotPassword;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequestDto {
    private String newPassword;
    private String token;
}
