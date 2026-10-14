package com.alinbudesabc.authenticator.controller;

import com.alinbudesabc.authenticator.BaseITest;
import com.alinbudesabc.authenticator.config.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ContextConfiguration(initializers = {BaseITest.Initializer.class})

public class UserControllerTest extends BaseITest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomUserDetailsService detailsService;


    @Autowired
    private PasswordEncoder passwordEncoder;



 //this method should be used together with @MockUser annotation in the testJwtGeneration() instead of @WithUserDetails
//    @BeforeEach
//    void setUp() {
//        detailsService.registerUser(RegisteredAminDto.builder()
//                        .username("testUser")
//                        .email("testUser@gmail.com")
//                        .password("testUser")
//                .build());
//    }

//    @BeforeAll
//    static void setUp(@Autowired CustomUserDetailsService detailsService) {
//        detailsService.registerUser(RegisteredUserDto.builder()
//                .username("testUser")
//                .email("testUser@gmail.com")
//                .password("testUser")
//                .build());
//    }



    //TODO: change this with another dto, UserDto is not used anymore
//    @Test
//    @WithMockUser(username = "testUser", roles = {"USER"})
////    @WithUserDetails(value = "testUser", userDetailsServiceBeanName = "customUserDetailsService")
//    public void testJwtGeneration() throws Exception{
//
//        MvcResult result = mockMvc.perform(get("/user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .servletPath("/user"))
////                .requestAttr("jakarta.servlet.include.servlet_path", "/user"))
//                .andExpect(status().isOk())
//                .andExpect(header().exists("Authorization"))
//                .andExpect(header().string("Authorization", notNullValue()))
//                .andReturn();
//
//        String responseBody = result.getResponse().getContentAsString();
//        UserDto userDto = objectMapper.readValue(responseBody, UserDto.class);
//        Assertions.assertEquals("testUser", userDto.username());
//        Assertions.assertEquals("testUser@gmail.com", userDto.email());
//    }
}