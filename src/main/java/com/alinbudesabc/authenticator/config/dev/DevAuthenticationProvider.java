package com.alinbudesabc.authenticator.config.dev;

import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;



//this class is not used anymore, we have ProdAuthenticationProvider
@Component
@RequiredArgsConstructor
//@Profile({"dev", "test"})
public class DevAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String pass = authentication.getCredentials().toString();
        //TODO: add a dto instead of userEntity ()
        //UserEntity user = userDetailsService.findUserByUsername(username);
//        List<SimpleGrantedAuthority> roles = user.getRoles().stream()
//                .map(roleEnum -> new SimpleGrantedAuthority(roleEnum.name()))
//                .toList();
//        if(passwordEncoder.matches(pass, user.getPassword()) && !user.isTwoFactorEnabled()){
//            return new UsernamePasswordAuthenticationToken(username, pass, roles);
//        }

//        if(passwordEncoder.matches(pass, user.getPassword()) && user.isTwoFactorEnabled()){
//            otpService.handleAuthentication(user);
//        }
            throw new BadCredentialsException("Invalid password");

    }


    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
