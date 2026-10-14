package com.alinbudesabc.authenticator.controller;

import com.alinbudesabc.authenticator.auth.util.RequestHeaderExtractor;
import com.alinbudesabc.authenticator.forgotPassword.ChangePasswordTokenService;
import com.alinbudesabc.authenticator.forgotPassword.PasswordResetRequestDto;
import com.alinbudesabc.authenticator.forgotPassword.PasswordResetService;
import com.alinbudesabc.authenticator.forgotPassword.PasswordResetTokenEntity;
import com.alinbudesabc.authenticator.model.dto.*;
import com.alinbudesabc.authenticator.auth.otp.OtpGoogleAuthenticatorStrategy;
import com.alinbudesabc.authenticator.auth.MSAuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RestLoginController {

    private final PasswordResetService resetService;
    private final ChangePasswordTokenService changePasswordTokenService;
    private final MSAuthenticationService authenticationService;
    private final OtpGoogleAuthenticatorStrategy googleAuthenticatorStrategy;
    private final RequestHeaderExtractor headerExtractor;


    @PostMapping("/login")
    public ResponseEntity<RESTLoginResponseDto> showLoginPage(@RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request) {
        RequestHeadersForJWT headers = headerExtractor.extract(request);
        RESTLoginResponseDto loginResponseDto = authenticationService.authenticateRESTService(loginRequestDto, headers);
        return ResponseEntity.ok(loginResponseDto);
    }


    @GetMapping(value = "/qrcode/get",  produces = MediaType.TEXT_PLAIN_VALUE)
    public byte[] getQrCode(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return  googleAuthenticatorStrategy.generateQRImageAndUpdateAccount(authentication.getName());
    }


    @PostMapping("/2fa/validate")
    public ResponseEntity<RESTLoginResponseDto> validate2FAToken(@RequestBody MfaVerificationRequestDto mfaVerificationRequestDto) {
        Pair<Boolean, String> authResult = authenticationService.validate2FATCode(mfaVerificationRequestDto);
        Boolean isValidCode = authResult.getFirst();
        if (isValidCode) {
            RESTLoginResponseDto loginResponseDto = new RESTLoginResponseDto(HttpStatus.OK.value(), authenticationService.getTokenForUser(mfaVerificationRequestDto.username()), null, true, mfaVerificationRequestDto.username());
            return ResponseEntity.ok(loginResponseDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new RESTLoginResponseDto(HttpStatus.UNAUTHORIZED.value(), null, mfaVerificationRequestDto.username(), false, mfaVerificationRequestDto.username()));
        }
    }


    @PostMapping("/forgotPassword")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam("email") String email, HttpServletRequest request){
        String passwordResetTokenMessage = resetService.createPasswordResetToken(email, request);
        return ResponseEntity.ok(Map.of("message", passwordResetTokenMessage));
    }


    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequestDto passwordRequest){
        PasswordResetTokenEntity tokenEntity = changePasswordTokenService.findByToken(passwordRequest.getToken());
        if(null == tokenEntity || changePasswordTokenService.isTokenExpired(tokenEntity)){
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid or expired token in this url");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        changePasswordTokenService.changePassword(tokenEntity, passwordRequest.getNewPassword());
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Password reset successfully.");
        return ResponseEntity.ok(response);
    }
}
