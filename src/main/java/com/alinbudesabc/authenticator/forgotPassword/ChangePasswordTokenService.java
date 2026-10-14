package com.alinbudesabc.authenticator.forgotPassword;

import com.alinbudesabc.authenticator.exception.PasswordChangeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangePasswordTokenService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;


    public String validatePasswordResetToken(String token) {
        Optional<PasswordResetTokenEntity>  passToken = tokenRepository.findByToken(token);

        return passToken.map(passwordResetTokenEntity -> isTokenExpired(passwordResetTokenEntity) ? "expired"
                : "validToken").orElse("invalidToken");
    }


    public boolean isTokenExpired(PasswordResetTokenEntity passToken) {
        return passToken.getExpirationTime().isBefore(LocalDateTime.now());
    }


    public PasswordResetTokenEntity findByToken(String token){
        return tokenRepository.findByToken(token).orElse(null);
    }

    public void changePassword(PasswordResetTokenEntity tokenEntity, String password){
        try{
            String hashedPAss = passwordEncoder.encode(password);
            tokenEntity.getUser().setPassword(hashedPAss);
        }
        catch (Exception e){
            log.error(e.getMessage());
            throw new PasswordChangeException("Error during password update process");
        }
        deletePasswordResetToken(tokenEntity);
    }

    public void deletePasswordResetToken(PasswordResetTokenEntity tokenEntity){
        tokenRepository.delete(tokenEntity);
    }
}

