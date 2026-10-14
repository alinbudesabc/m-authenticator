package com.alinbudesabc.authenticator.persistance.repositories;

import com.alinbudesabc.authenticator.enums.RoleEnum;
import com.alinbudesabc.authenticator.persistance.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesRepository extends JpaRepository<RoleEntity, Long> {

    @Query("""
            SELECT r
            FROM RoleEntity r 
            WHERE r.roleName IN 
            :roles
            """)
    List<RoleEntity> findAllByRoleName(@Param("roles") List<RoleEnum> roles);
}
