package com.alinbudesabc.authenticator.model.dto;

import com.alinbudesabc.authenticator.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredAminDto {

    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private List<RoleEnum> roles;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

}
