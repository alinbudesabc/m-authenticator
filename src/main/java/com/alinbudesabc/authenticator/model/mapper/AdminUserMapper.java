package com.alinbudesabc.authenticator.model.mapper;


import com.alinbudesabc.authenticator.model.dto.RegisteredAminDto;
import com.alinbudesabc.authenticator.persistance.entities.RoleEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserRoleEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AdminUserMapper {

    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "isAccountNonExpired", constant = "true")
    @Mapping(target = "isAccountNonLocked", constant = "true")
    @Mapping(target = "isCredentialsNonExpired", constant = "true")
    @Mapping(target = "isEnabled", constant = "true")
    @Mapping(target = "authStrategy", constant = "ONE_STEP_AUTH")
    @Mapping(target = "twoFactorMessageStrategy", constant = "NONE")
    UserEntity toEntity(RegisteredAminDto dto, List<RoleEntity> roleEntities);


    @AfterMapping
    default void mapRoles(RegisteredAminDto dto, @MappingTarget UserEntity userEntity, List<RoleEntity> rolesFromDb) {
        if (null != rolesFromDb) {
            Set<UserRoleEntity> userRoles = rolesFromDb
                    .stream()
                    .map(roleEntity -> new UserRoleEntity(userEntity, roleEntity))
                    .collect(Collectors.toSet());
            userEntity.setUserRoles(userRoles);
        } else {
            System.out.println("Missing roles from db");;
        }
    }
}
