package com.alinbudesabc.authenticator.controller;

import com.alinbudesabc.authenticator.BaseITest;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.alinbudesabc.authenticator.model.dto.LoginRequestDto;
import com.alinbudesabc.authenticator.model.dto.MfaVerificationRequestDto;
import com.alinbudesabc.authenticator.model.dto.RegisteredAminDto;
import com.alinbudesabc.authenticator.model.mapper.AdminUserMapper;
import com.alinbudesabc.authenticator.persistance.entities.RoleEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import com.alinbudesabc.authenticator.persistance.repositories.RolesRepository;
import com.alinbudesabc.authenticator.persistance.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureMockMvc()
@ContextConfiguration(initializers = {BaseITest.Initializer.class})
public class RestLoginControllerTestIT extends BaseITest{

    private static final String USERNAME = "testUser";
    private static String PASSWORD = "testPassword";
    private static String TFA_KEY = "testKey";
    private static String WRONG_TFA_CODE = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        RegisteredAminDto dto = RegisteredAminDto.builder()
                .username(USERNAME)
                .firstName("test")
                .lastName("user")
                .email("testEmail")
                .password(PASSWORD)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .roles(List.of(RoleEnum.ROLE_ADMIN))
                .build();


        List<RoleEntity> roleEntities = rolesRepository.findAllByRoleName(dto.getRoles());
        String hashedPAss = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(hashedPAss);

        UserEntity userEntity = adminUserMapper.toEntity(dto, roleEntities);
        userEntity.setTwoFactorEnabled(true);
        userEntity.setTwoFactorMessageStrategy(MessagingStrategyEnum.AUTHENTICATOR);
        userEntity.setAuthStrategy(AuthTypeEnum.TWO_STEP_AUTH);
        userEntity.setMfaSecret(TFA_KEY);

        userRepository.save(userEntity);
    }

//    @Test
//    void testTFAValidationWhenTFACodeOK() throws Exception {
//       performFirstStepAuthentication();
//
//        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
//        int code = googleAuthenticator.getTotpPassword(TFA_KEY);
//        MfaVerificationRequestDto requestDto = new MfaVerificationRequestDto(USERNAME, String.valueOf(code));
//
//        mockMvc.perform(post("/2fa/validate")
//                .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.username").value(USERNAME));
//    }

    @Test
    void testTFAValidationWhenTFACodeNOK() throws Exception {
        performFirstStepAuthentication();

        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        String code = WRONG_TFA_CODE;
        MfaVerificationRequestDto requestDto = new MfaVerificationRequestDto(USERNAME, code);

        mockMvc.perform(post("/2fa/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.username").value(USERNAME));
    }



    private void performFirstStepAuthentication() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto(USERNAME, PASSWORD);
        ResultActions resultActions = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.jwtToken").doesNotExist());

    }

}