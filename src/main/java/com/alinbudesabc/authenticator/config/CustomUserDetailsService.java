package com.alinbudesabc.authenticator.config;

import com.alinbudesabc.authenticator.brutforceattak.LoginAttemptService;
import com.alinbudesabc.authenticator.exception.UserAlreadyExistsException;
import com.alinbudesabc.authenticator.model.dto.RegisteredAminDto;
import com.alinbudesabc.authenticator.model.dto.TwoFactorDto;
import com.alinbudesabc.authenticator.model.dto.UserWithRolesDto;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import com.alinbudesabc.authenticator.model.mapper.AdminUserMapper;
import com.alinbudesabc.authenticator.persistance.entities.RoleEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import com.alinbudesabc.authenticator.persistance.repositories.RolesRepository;
import com.alinbudesabc.authenticator.persistance.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserMapper adminUserMapper;
    //create service for roleRepo and inject it here instead of repo
    private final RolesRepository rolesRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user for this username: " + username));

        List<SimpleGrantedAuthority> roles = userEntity.getRoles().stream()
                .map(roleEnum -> new SimpleGrantedAuthority(roleEnum.name()))
                .toList();

        return new User(userEntity.getUsername(), userEntity.getPassword(), roles);
     }

//    public void handleUserStateWhenLoginAttempt(UserEntity userEntity) {
//        if(loginAttemptService.isProposedForBlacklist(userEntity.getUsername())) {
//            userEntity.setAccountNonLocked(false);
//            //TODO: userEntity.setLastLoginDate
//            //TODO: see if I can just update the field in the db, not the entire object
//            userRepository.save(userEntity);
////            throw new TemporallyBlockedAccountException("Account temporarily locked due to too many failed login attempts.");
//            throw new BlockedUserException("Account locked.");
//        }
//    }

    public UserEntity findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with this email"));
     }


    @Transactional
    public UserEntity registerUser(RegisteredAminDto dto) {

        Optional<UserEntity> optionalUserByEmail = userRepository.findByEmail(dto.getEmail());
        Optional<UserEntity> optionalUserByUsername = userRepository.findByUsername(dto.getUsername());

        if (optionalUserByEmail.isPresent() || optionalUserByUsername.isPresent()) {
            throw new UserAlreadyExistsException("Email or username already exists.");
        }


        //TODO:don't throw exception, create user just with base role(USER ROLE)
        //TODO: maybe create another method for adding roles(option for adding roles to users should be restricted to an ADMIN role )
        //TODO: create this logic
        if(dto.getRoles().isEmpty()){
            throw new RuntimeException("Dto received has no roles");
        }

        //TODO: do we want to assign all roles for a user? think of a strategy for how many roles should a new user have. maybe just  USER role
        List<RoleEntity> roleEntities = rolesRepository.findAllByRoleName(dto.getRoles());
        if(roleEntities.isEmpty()){
            throw new IllegalArgumentException("Roles " + dto.getRoles() + " does not exist in the db. You should create them first");
        }

        String hashedPAss = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(hashedPAss);
        UserEntity userEntity = adminUserMapper.toEntity(dto, roleEntities);
        return userRepository.save(userEntity);
    }


    public List<UserEntity> getUsers() {
        return userRepository.findAllWithRoles();
    }


    public UserDetailsDto getUserDetailsDto(String username) {
        return userRepository.findUserDetails(username);
    }

    public UserWithRolesDto getUserWithRoles(String username){
        UserEntity userEntity = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user for this username: " + username));

        List<SimpleGrantedAuthority> roles = userEntity.getRoles().stream()
                .map(roleEnum -> new SimpleGrantedAuthority(roleEnum.name()))
                .toList();

        return UserWithRolesDto.builder()
                .password(userEntity.getPassword())
                .username(username)
                .isAccountNonBlocked(userEntity.isAccountNonLocked())
                .roles(roles)
                .build();
    }

    public void updateAccountNotLockedDB(String username, boolean isAccountNonBlocked){
        userRepository.updateAccountNonLockedByUsername(username, isAccountNonBlocked);
    }

    public UserEntity findUserByUsername(String username){
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Did not find this username"));
    }

    public void updateAccountWith(TwoFactorDto twoFactorDto) {
        userRepository.updateUserEntityBy (
                twoFactorDto.twoFactorEnabled(),
                twoFactorDto.authTypeEnum(),
                twoFactorDto.messagingStrategyEnum(),
                twoFactorDto.secretTfaKey(),
                twoFactorDto.username());
    }

    public List<String> getBalcklistedUsers() {
        return userRepository.findBlacklistedUsers();
    }

    public void enablePermanentlyBlockedAccount(String username){
        loginAttemptService.enablePermanentlyBlockedAccount(username,
                (enable) -> userRepository.updateAccountNonLockedByUsername(username, enable));
    }
    public void deleteUserById(Long id){
        if (null == id){
            throw new IllegalArgumentException("Missing id");
        }
        userRepository.deleteById(id);
    }
}
