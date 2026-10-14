package com.alinbudesabc.authenticator.controller;

import com.alinbudesabc.authenticator.BaseITest;
import com.alinbudesabc.authenticator.auth.TokenGenerator;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.alinbudesabc.authenticator.model.dto.RegisteredAminDto;
import com.alinbudesabc.authenticator.model.mapper.AdminUserMapper;
import com.alinbudesabc.authenticator.persistance.entities.RoleEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import com.alinbudesabc.authenticator.persistance.repositories.RolesRepository;
import com.alinbudesabc.authenticator.persistance.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


@AutoConfigureMockMvc()
@ContextConfiguration(initializers = {BaseITest.Initializer.class})
class UserControllerTestIT extends BaseITest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;


    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserMapper adminUserMapper;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        RegisteredAminDto dto = RegisteredAminDto.builder()
                .username("admin")
                .firstName("test")
                .lastName("user")
                .email("testEmail")
                .password("admin123")
                .accountNonExpired(true)
                .accountNonLocked(true)
                .roles(List.of(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_USER))
                .build();


        List<RoleEntity> roleEntities = List.of(new RoleEntity(RoleEnum.ROLE_USER), new RoleEntity(RoleEnum.ROLE_ADMIN));
        String hashedPAss = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(hashedPAss);

        UserEntity userEntity = adminUserMapper.toEntity(dto, roleEntities);
        userEntity.setTwoFactorEnabled(true);
        userEntity.setTwoFactorMessageStrategy(MessagingStrategyEnum.AUTHENTICATOR);
        userEntity.setAuthStrategy(AuthTypeEnum.TWO_STEP_AUTH);
        userEntity.setMfaSecret("tfaKey");

        userRepository.save(userEntity);
    }


//    @Test
//    void testUserRegistrationByAdmin() throws Exception {
//        RequestHeadersForJWT reqHeaders = RequestHeadersForJWT.builder().ipAddress("127.0.0.1").build();
//        UserEntity usr = userRepository.findByUsername("admin").get();
////        System.out.println("------------------------------------------SOUT----");
////        System.out.println(usr);
//        // create authentication
//        // add token
//        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(RoleEnum.ROLE_ADMIN.name()));
//        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin123", authorities);
//        //we need token in the header because this admin/register path is a protected path. You need ADMIN role
//        String token = tokenGenerator.generateToken(authentication, reqHeaders);
//        RegisteredAminDto account = createAccount();
//        mockMvc.perform(post("/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", token)
//                        .content(objectMapper.writeValueAsString(account)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value(201))
//                .andExpect(content().string("User successfully created: " + account.getUsername()));
//
//        UserEntity userEntity = userRepository.findByUsername(account.getUsername()).get();
//        assertEquals("testUser", userEntity.getUsername(), "Account created with username: "+ userEntity.getUsername());
//    }

    private RegisteredAminDto createAccount(){
            return RegisteredAminDto.builder()
                    .username("testUser")
                    .password("testPassword")
                    .email("admin@example.com")
                    .firstName("Alice")
                    .lastName("Admin")
                    .roles(List.of(RoleEnum.ROLE_ADMIN, RoleEnum.ROLE_USER))
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .enabled(true)
                    .build();
    }

}