package com.alinbudesabc.authenticator.persistance.repositories;

import com.alinbudesabc.authenticator.persistance.entities.UserRoleEntity;
import com.alinbudesabc.authenticator.persistance.entities.UserRoleKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRolesRepository extends CrudRepository<UserRoleEntity, UserRoleKey> {

}
