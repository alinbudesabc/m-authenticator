package com.alinbudesabc.authenticator.service;

import com.alinbudesabc.authenticator.auth.MSAuthenticationService;
import com.alinbudesabc.authenticator.auth.TokenGenerator;
import com.alinbudesabc.authenticator.auth.otp.OtpService;
import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.alinbudesabc.authenticator.dto.UserDetailsDtoImpl;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.*;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MSAuthenticationServiceTest{

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private MSAuthenticationService msAuthenticationService;


    @Test
    void testIfAuthenticatedForOneStepAuth() {
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);

        RequestHeadersForJWT reqHeaders = RequestHeadersForJWT.builder().ipAddress("127.0.0.1").build();

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(userDetailsService.getUserDetailsDto(buildRequest().username()))
                .thenReturn(createUserDetailWithOneStepAuth());
        when(tokenGenerator.generateToken(mockAuth, reqHeaders)).thenReturn("testtoken");

        RESTLoginResponseDto response = msAuthenticationService.authenticateRESTService(buildRequest(), reqHeaders);


//        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
//
//        verify(authenticationManager).authenticate(captor.capture());

//        Authentication actualAuth = captor.getValue();
//        System.out.println("Captured Authentication: " + actualAuth);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());
        assertEquals("testtoken", response.jwtToken());
        assertEquals(MSAuthenticationService.BASIC_AUTH_SUCCESS, response.message());
        assertFalse(response.tfaEnabled());
    }

    @Test
    void testIfAuthenticatedFor2StepAuth() {
        RequestHeadersForJWT reqHeaders = RequestHeadersForJWT.builder().ipAddress("127.0.0.1").build();
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(userDetailsService.getUserDetailsDto(buildRequest().username()))
                .thenReturn(createUserDetailWith2StepAuth());
        when(tokenGenerator.generateToken(mockAuth, reqHeaders)).thenReturn("testtoken");

        RESTLoginResponseDto response = msAuthenticationService.authenticateRESTService(buildRequest(), reqHeaders);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.status());
        assertNull(response.jwtToken());
        assertEquals(MSAuthenticationService.TWO_STEP_AUTH_DETECTED, response.message());
        assertTrue(response.tfaEnabled());
    }


    @Test
    void testNotAuthenticated() {
        RequestHeadersForJWT reqHeaders = RequestHeadersForJWT.builder().ipAddress("127.0.0.1").build();
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(false);

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        RESTLoginResponseDto response = msAuthenticationService.authenticateRESTService(buildRequest(), reqHeaders);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.status());
        assertNull(response.jwtToken());
        assertEquals(MSAuthenticationService.AUTH_FAILED, response.message());
        assertFalse(response.tfaEnabled());
    }


    @Test
    void checkTFAValidWhenTfaCodeValid() throws Exception {
        MfaVerificationRequestDto twoStepValidationRequest = new MfaVerificationRequestDto("testUser", "123456");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(mockAuth);
        SecurityContextHolder.setContext(securityContext);

        Map<String, String> mockGeneratedTokens = new ConcurrentHashMap<>();
        mockGeneratedTokens.put("testUser", "someToken");
        Field generatedTokensField = MSAuthenticationService.class.getDeclaredField("generatedTokens");
        generatedTokensField.setAccessible(true);
        generatedTokensField.set(msAuthenticationService, mockGeneratedTokens);

        when(userDetailsService.getUserDetailsDto(twoStepValidationRequest.username()))
                .thenReturn(createUserDetailWith2StepAuth());

        when(otpService.handleAuthentication(createUserDetailWith2StepAuth(), twoStepValidationRequest.tfaCode())).thenReturn(Pair.of(true, "Code validated"));

        Pair<Boolean, String> validation = msAuthenticationService.validate2FATCode(twoStepValidationRequest);

        assertTrue(validation.getFirst());
        assertEquals("Code validated", validation.getSecond());
    }

    @Test
    void checkTFAValidWhenWrongTFACode() throws Exception {
        MfaVerificationRequestDto twoStepValidationRequest = new MfaVerificationRequestDto("testUser", "123");
        Map<String, String> mockGeneratedTokens = new ConcurrentHashMap<>();
        mockGeneratedTokens.put("testUser", "someToken");
        Field generatedTokensField = MSAuthenticationService.class.getDeclaredField("generatedTokens");
        generatedTokensField.setAccessible(true);
        generatedTokensField.set(msAuthenticationService, mockGeneratedTokens);

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(mockAuth);
        SecurityContextHolder.setContext(securityContext);

        when(userDetailsService.getUserDetailsDto(twoStepValidationRequest.username()))
                .thenReturn(createUserDetailWith2StepAuth());

        when(otpService.handleAuthentication(createUserDetailWith2StepAuth(), twoStepValidationRequest.tfaCode())).thenReturn(Pair.of(false, "Code not valid"));

        Pair<Boolean, String> validation = msAuthenticationService.validate2FATCode(twoStepValidationRequest);
        assertFalse(validation.getFirst());
        assertEquals("Code not valid", validation.getSecond());
    }

    @Test
    void checkTFACodeWhenUSerNotBAsicAuthenticated() throws Exception {
        MfaVerificationRequestDto twoStepValidationRequest = new MfaVerificationRequestDto("testUser", "123456");

        Authentication mockAuth = mock(Authentication.class);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(mockAuth);
        SecurityContextHolder.setContext(securityContext);

        when(userDetailsService.getUserDetailsDto(twoStepValidationRequest.username()))
                .thenReturn(createUserDetailWith2StepAuth());

        Pair<Boolean, String> validation = msAuthenticationService.validate2FATCode(twoStepValidationRequest);

        assertFalse(validation.getFirst());
        assertEquals("Basic authentication needed", validation.getSecond());
    }



    private static LoginRequestDto buildRequest(){
        return LoginRequestDto
                .builder()
                .password("testPass")
                .username("testUser")
                .build();
    }

    private static UserWithRolesDto buildUser(){
       return  UserWithRolesDto.builder()
                .roles(List.of(new SimpleGrantedAuthority("ADMIN")))
                .build();
    }


    private static UserDetailsDto createUserDetailWithOneStepAuth() {
        return UserDetailsDtoImpl.builder()
                .username("testuser")
                .email("test@example.com")
                .twoFactorEnabled(false)
                .twoFactorMessageStrategy(MessagingStrategyEnum.NONE)
                .authStrategy(AuthTypeEnum.ONE_STEP_AUTH)
                .mfaSecret(null)
                .isAccountNonLocked(true)
                .build();
    }

    private static UserDetailsDto createUserDetailWith2StepAuth() {
        return UserDetailsDtoImpl.builder()
                .username("testuser")
                .email("test@example.com")
                .twoFactorEnabled(true)
                .twoFactorMessageStrategy(MessagingStrategyEnum.AUTHENTICATOR)
                .authStrategy(AuthTypeEnum.TWO_STEP_AUTH)
                .mfaSecret("secret-key")
                .isAccountNonLocked(true)
                .build();
    }


}