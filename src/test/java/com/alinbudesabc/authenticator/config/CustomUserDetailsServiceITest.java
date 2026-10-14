package com.alinbudesabc.authenticator.config;

import com.alinbudesabc.authenticator.BaseITest;
import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.alinbudesabc.authenticator.model.dto.RegisteredAminDto;
import com.alinbudesabc.authenticator.model.mapper.AdminUserMapper;
import com.alinbudesabc.authenticator.persistance.entities.RoleEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserRoleEntity;
import com.alinbudesabc.authenticator.persistance.repositories.RolesRepository;
import com.alinbudesabc.authenticator.persistance.repositories.UserRepository;
import com.alinbudesabc.authenticator.persistance.repositories.UserRolesRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ContextConfiguration(initializers = {BaseITest.Initializer.class})
class CustomUserDetailsServiceITest extends BaseITest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup(){
        rolesRepository.saveAll(List.of(new RoleEntity(RoleEnum.ROLE_USER),new RoleEntity(RoleEnum.ROLE_ADMIN)));
        rolesRepository.flush();
    }

    @Test
    @DisplayName("Test if user is successfully saved")
    @Transactional
    void loadUserByUsername() {
        RegisteredAminDto requestUserDto = getRequestAdminDto();
        RoleEntity roleEntity = rolesRepository.findAll().stream()
                .filter(role -> RoleEnum.ROLE_USER == role.getRoleName())
                .findAny()
                .get();
        UserEntity entity = adminUserMapper.toEntity(requestUserDto, List.of(roleEntity));
        userRepository.save(entity);

        UserDetails userDetails = userDetailsService.loadUserByUsername("test_username");

        assertEquals("test_username", userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(role -> "ROLE_USER".equals(role.getAuthority())),
                "User should have ROLE_USER");
        userRepository.deleteAll();
        userRolesRepository.deleteAll();
        rolesRepository.deleteAll();
    }

    @Test
    void testRegisterUser() {
        RegisteredAminDto requestUserDto = getRequestAdminDto();

        userDetailsService.registerUser(requestUserDto);

        UserEntity userByEmail = userDetailsService.findUserByEmail("test@email.com");

        Assertions.assertEquals("test_username", userByEmail.getUsername());
        Assertions.assertEquals(1, userByEmail.getRoles().size());
        Assertions.assertEquals(AuthTypeEnum.ONE_STEP_AUTH, userByEmail.getAuthStrategy());
        Assertions.assertEquals(MessagingStrategyEnum.NONE, userByEmail.getTwoFactorMessageStrategy());
        assertTrue(userByEmail.getRoles().stream()
                .anyMatch(role -> role.name().equals(RoleEnum.ROLE_USER.name())), "role USER should be present");
        userRepository.deleteAll();
        userRolesRepository.deleteAll();
        rolesRepository.deleteAll();
    }

    @Transactional
    @Test
    void getUsersTest() {
        Set<RoleEnum> expectedRoles = Set.of(RoleEnum.ROLE_USER);
        RoleEntity roleEntity = rolesRepository.findAll().stream()
                .filter(role -> RoleEnum.ROLE_USER == role.getRoleName())
                .findAny()
                .get();

        List<UserEntity> userEntities =
                buildUsers().stream()
                        .map(registeredUserDto -> adminUserMapper.toEntity(registeredUserDto, List.of(roleEntity)))
                        .toList();

      userRepository.saveAll(userEntities);

      List<UserEntity> users = userDetailsService.getUsers();

      assertThat(users).extracting(
                        UserEntity::getUsername,
                        UserEntity::getEmail,
                        UserEntity::getRoles,
                        UserEntity::getTwoFactorMessageStrategy,
                        UserEntity::getAuthStrategy,
                        UserEntity::isTwoFactorEnabled
                      )
                .containsExactlyInAnyOrder(
                  tuple("test_username", "test@email.com", expectedRoles, MessagingStrategyEnum.NONE, AuthTypeEnum.ONE_STEP_AUTH, false ),
                  tuple("test2_username", "test2@email.com",expectedRoles, MessagingStrategyEnum.NONE, AuthTypeEnum.ONE_STEP_AUTH, false )
                );
        userRepository.deleteAll();
        userRolesRepository.deleteAll();
        rolesRepository.deleteAll();
    }

    @Test
    void testSideEffectsWhenRegisterUser(){
        Set<RoleEnum> roles = Set.of(RoleEnum.ROLE_USER);
        List<RoleEntity> rolesBeforeRegistration = rolesRepository.findAll();
        List<UserRoleEntity> userRolesBeforeRegistration = (List<UserRoleEntity>) userRolesRepository.findAll();
        List<UserEntity> usersBeforeRegistration = userRepository.findAll();

        assertEquals(2, rolesBeforeRegistration.size());
        assertEquals(0, userRolesBeforeRegistration.size());
        assertEquals(0, usersBeforeRegistration.size());

        userDetailsService.registerUser(getRequestAdminDto());

        List<RoleEntity> rolesAfterRegistration = rolesRepository.findAll();
        List<UserRoleEntity> userRolesAfterRegistration = (List<UserRoleEntity>) userRolesRepository.findAll();
        List<UserEntity> usersAfterRegistration = userDetailsService.getUsers();

        assertEquals(2, rolesAfterRegistration.size());
        assertEquals(1, userRolesAfterRegistration.size());
        assertThat(usersAfterRegistration).hasSize(1).extracting(
                        UserEntity::getUsername,
                        UserEntity::getRoles)
                .containsExactlyInAnyOrder(
                        tuple("test_username", roles )
//                        tuple("test2_username",roles )
                );
        userRepository.deleteAll();
        userRolesRepository.deleteAll();
        rolesRepository.deleteAll();
    }

    //verify if deleting a user caused any orphaned references in related binding tables
    @Test
    void testDeleteByIdForSideEffects(){
//        rolesRepository.saveAllAndFlush(List.of(new RoleEntity(RoleEnum.ROLE_USER), new RoleEntity(RoleEnum.ROLE_ADMIN)));
        List<RoleEntity> rolesBeforeRegistration = rolesRepository.findAll();
        List<UserRoleEntity> userRolesBeforeRegistration = (List<UserRoleEntity>) userRolesRepository.findAll();
        assertEquals(2, rolesBeforeRegistration.size());
        assertEquals(0, userRolesBeforeRegistration.size());

        //register new user
        UserEntity dbUser = userDetailsService.registerUser(getRequestAdminDto());

        List<RoleEntity> rolesAfterRegistration = rolesRepository.findAll();
        List<UserRoleEntity> userRolesAfterRegistration = (List<UserRoleEntity>) userRolesRepository.findAll();
        assertEquals(2, rolesAfterRegistration.size());
        assertEquals(1, userRolesAfterRegistration.size());
        assertThat(dbUser.getRoles())
                .hasSize(1)
                .contains(RoleEnum.ROLE_USER);

        userDetailsService.deleteUserById(dbUser.getId());
        List<UserRoleEntity> userRolesAfterDeletedUser = (List<UserRoleEntity>) userRolesRepository.findAll();
        List<RoleEntity> rolesAfterDeletedUser = rolesRepository.findAll();
        List<UserEntity> users = userRepository.findAll();
        assertEquals(0, users.size());
        assertEquals(2, rolesAfterDeletedUser.size());
        assertEquals(0, userRolesAfterDeletedUser.size());

//      clean db
        userRepository.deleteAll();
        userRolesRepository.deleteAll();
        rolesRepository.deleteAll();
    }


    private static RegisteredAminDto getRequestAdminDto(){
        return RegisteredAminDto.builder()
                .username("test_username")
                .email("test@email.com")
                .firstName("FirstName")
                .lastName("LastName")
                .password("test123")
                .roles(List.of(RoleEnum.ROLE_USER))
                .build();
    }

    private static List<RegisteredAminDto> buildUsers(){
        return List.of(getRequestAdminDto(),
                RegisteredAminDto.builder()
                        .username("test2_username")
                        .email("test2@email.com")
                        .firstName("FirstName2")
                        .lastName("LatName2")
                        .password("test123")
//                        .roles(List.of(RoleEnum.ROLE_USER))
                        .build()
        );
    }
}