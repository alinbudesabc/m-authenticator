package com.alinbudesabc.authenticator.persistance.repositories;

import com.alinbudesabc.authenticator.enums.AuthTypeEnum;
import com.alinbudesabc.authenticator.enums.MessagingStrategyEnum;
import com.alinbudesabc.authenticator.model.dto.projection.UserDetailsDto;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("""
            SELECT u FROM UserEntity u
            LEFT JOIN FETCH u.userRoles ur
            LEFT JOIN FETCH ur.role
            WHERE u.username = :username
            """)
    Optional<UserEntity> findByUsername(@Param("username") String username);


    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role")
    List<UserEntity> findAllWithRoles();


    @Query("""
            SELECT u FROM UserEntity u
            LEFT JOIN FETCH u.userRoles ur
            LEFT JOIN FETCH ur.role
            WHERE u.email = :email
            """)
    Optional<UserEntity> findByEmail(@Param("email") String email);


    @Modifying
    @Query(""" 
            UPDATE UserEntity u
            SET u.mfaSecret = :key,
            u.twoFactorMessageStrategy = :mfaStrategy,
            u.authStrategy = :authType,
            u.twoFactorEnabled = :tfaEnabled
            WHERE u.username = :username
            """)
    int updateAccount(@Param("key") String key,
                      @Param("mfaStrategy") MessagingStrategyEnum mfaStrategy,
                      @Param("authType") AuthTypeEnum authType,
                      @Param("tfaEnabled") boolean tfaEnabled,
                      @Param("username") String username);



    @Query("""
            SELECT u.username AS username,
            u.email AS email,
            u.twoFactorEnabled AS twoFactorEnabled,
            u.twoFactorMessageStrategy AS twoFactorMessageStrategy,
            u.mfaSecret AS mfaSecret,
            u.isAccountNonLocked AS isAccountNonLocked,
            u.authStrategy AS authStrategy
            FROM UserEntity u
            WHERE u.username = :username
            """)
    UserDetailsDto findUserDetails(@Param("username") String username);

    @Transactional
    @Modifying
    @Query("UPDATE UserEntity u SET u.isAccountNonLocked = :accountNonLocked WHERE u.username = :username")
    void updateAccountNonLockedByUsername(@Param("username") String username, @Param("accountNonLocked") boolean accountNonLocked);

    @Transactional
    @Modifying
    @Query("""
            UPDATE UserEntity u
            SET u.twoFactorEnabled = :tfaEnabled,
            u.authStrategy = :authType,
            u.twoFactorMessageStrategy = :tfaMessagingStrategy,
            u.mfaSecret =:secretKey
            WHERE u.username = :username
            """)
    void updateUserEntityBy(@Param("tfaEnabled") boolean tfaEnabled,
                            @Param("authType") AuthTypeEnum authType,
                            @Param("tfaMessagingStrategy") MessagingStrategyEnum tfaMessagingStrategy,
                            @Param("secretKey") String secretKey,
                            @Param("username") String username
                            );

    @Query(
            """
                SELECT u.username
                FROM UserEntity u
                WHERE u.isAccountNonLocked = false
            """)
    List<String> findBlacklistedUsers();
}
