package com.alinbudesabc.authenticator.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;


@Getter
@Setter
@Builder
public class UserWithRolesDto {
    private String username;
    private String password;
    private boolean isAccountNonBlocked;
    private List<SimpleGrantedAuthority> roles;
}
