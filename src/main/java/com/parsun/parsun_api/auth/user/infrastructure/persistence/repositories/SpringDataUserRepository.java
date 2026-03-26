package com.parsun.parsun_api.auth.user.infrastructure.persistence.repositories;

import com.parsun.parsun_api.auth.user.infrastructure.persistence.entities.UserEntity;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByRole(UserRole role);
}
