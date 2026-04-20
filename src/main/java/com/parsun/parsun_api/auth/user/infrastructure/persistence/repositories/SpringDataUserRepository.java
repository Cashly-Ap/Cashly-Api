package com.parsun.parsun_api.auth.user.infrastructure.persistence.repositories;

import com.parsun.parsun_api.auth.user.infrastructure.persistence.entities.UserEntity;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByRole(UserRole role);

    @Query("""
        SELECT u FROM UserEntity u
        WHERE (:search IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:role IS NULL OR u.role = :role)
        AND (:active IS NULL OR u.active = :active)
        AND (:excludeId IS NULL OR u.id != :excludeId)
        """)
    Page<UserEntity> findAllWithFilters(
        @Param("search") String search,
        @Param("role") UserRole role,
        @Param("active") Boolean active,
        @Param("excludeId") UUID excludeId,
        Pageable pageable
    );
}

