package com.alinbudesabc.authenticator.auth;

import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.LoginRequestDto;
import com.alinbudesabc.authenticator.model.dto.MfaVerificationRequestDto;
import com.alinbudesabc.authenticator.model.dto.RESTLoginResponseDto;
import com.alinbudesabc.authenticator.model.dto.RequestHeadersForJWT;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import com.alinbudesabc.authenticator.auth.otp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MSAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final TokenGenerator tokenGenerator;
    private final OtpService otpService;
    private final CustomUserDetailsService detailsService;
    private final Map<String, String> generatedTokens = new ConcurrentHashMap<>();//keeps tokens after basic authentication succeeded.need them later in the 2step auth:  code validation

    public static final String BASIC_AUTH_SUCCESS = "Basic authentication succeeded";
    public static final String AUTH_FAILED = "Authentication failed";
    public static final String BASIC_AUTH_NEEDED = "Basic authentication needed";
    public static final String USER_NOT_AUTHENTICATED = "User not authenticated";
    public static final String TWO_STEP_AUTH_DETECTED = "Two step auth detected";


    //authenticate the user and return the jwt and tfaEnabled
    //if tfaEnabled == true then the frontend should continue the authentication process with a two-factor authentication request
    public RESTLoginResponseDto authenticateRESTService(LoginRequestDto loginRequestDto, RequestHeadersForJWT headers){
        UserDetailsDto userDetailsDto;
        UsernamePasswordAuthenticationToken unauthenticated = UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequestDto.username(), loginRequestDto.password());

        Authentication authenticate = authenticationManager.authenticate(unauthenticated);

        if (null != authenticate && authenticate.isAuthenticated()) {
            userDetailsDto = detailsService.getUserDetailsDto(loginRequestDto.username());
            String refreshToken = tokenGenerator.generateRefreshToken(authenticate, headers);
            String accessToken = tokenGenerator.generateAccessToken(authenticate, headers);
            SecurityContextHolder.getContext().setAuthentication(authenticate);

            // if the user has the tfa enabled then we don't send the token at this step, we save it for the additional step
            // there is an additional step in the auth process(initiated from frontend) where we add it to the response
            if(userDetailsDto.getTwoFactorEnabled() && AuthTypeEnum.TWO_STEP_AUTH == userDetailsDto.getAuthStrategy() && MessagingStrategyEnum.AUTHENTICATOR == userDetailsDto.getTwoFactorMessageStrategy()) {
                generatedTokens.put(userDetailsDto.getUsername(), accessToken);
                return new RESTLoginResponseDto(HttpStatus.OK.value(), null, null, userDetailsDto.getTwoFactorEnabled(), TWO_STEP_AUTH_DETECTED);
            }

            //return the token if it's just the base authentication enabled(ONE STEP AUTH)
            if(!userDetailsDto.getTwoFactorEnabled() && AuthTypeEnum.ONE_STEP_AUTH == userDetailsDto.getAuthStrategy()) {
                return new RESTLoginResponseDto(HttpStatus.OK.value(), accessToken, refreshToken, userDetailsDto.getTwoFactorEnabled(), BASIC_AUTH_SUCCESS);
            }
        }
        return new RESTLoginResponseDto(HttpStatus.UNAUTHORIZED.value(), null, null, false, AUTH_FAILED);
    }

    public Pair<Boolean, String> validate2FATCode(MfaVerificationRequestDto mfaVerificationRequestDto) {
        UserDetailsDto twoFactorDetails = detailsService.getUserDetailsDto(mfaVerificationRequestDto.username());

        if (null == twoFactorDetails || generatedTokens.get(mfaVerificationRequestDto.username()) == null){
            log.warn("There is no basic authentication executed or no token cached after basic authentication for user: {}", mfaVerificationRequestDto.username());
            return Pair.of(false, BASIC_AUTH_NEEDED);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("User from security context: {}", authentication.getName());
        if (authentication.isAuthenticated()){
            return otpService.handleAuthentication(twoFactorDetails, mfaVerificationRequestDto.tfaCode());
        }

        return Pair.of(false, USER_NOT_AUTHENTICATED);
    }

    //get token then remove it
    public String getTokenForUser(String username) {
        return generatedTokens.remove(username);
    }

}
