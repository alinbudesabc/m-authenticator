package com.alinbudesabc.authenticator.forgotPassword;


import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.constants.AppConstants;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    @Value("${app.protocol}")
    private String protocol;

    private final PasswordResetTokenRepository tokenRepository;
    private final CustomUserDetailsService userService;
    private final JavaMailSender mailSender;

    public String createPasswordResetToken(String email, HttpServletRequest request){
        UserEntity user = userService.findUserByEmail(email);
        String token = UUID.randomUUID().toString();
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity(token, user);
        LocalDateTime now = LocalDateTime.now();
        tokenEntity.setExpirationTime(now.plusHours(AppConstants.RESET_PASS_EXPIRATION_TIME));
        tokenRepository.save(tokenEntity);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // TODO: change here, this is temporarily until we set an email server
            message.setFrom("alinbudesabc@gmail.com");
//            message.setTo("alinbudesabc@gmail.com");
            message.setTo(email);
            message.setSubject("Reset password");
            message.setText(generateLink(token, request));
            mailSender.send(message);
        } catch (Exception e) {
            return e.getMessage();
        }
        return AppConstants.RESET_PASS_EMAIL_SENT;
    }

    private String generateLink(String token, HttpServletRequest request){
        return rootLink(request) + "/resetPassword2.html?token=" + token;
    }

    private String rootLink(HttpServletRequest request) {
        return protocol + request.getServerName()+":"
                + request.getServerPort() + request.getContextPath();
    }
}
