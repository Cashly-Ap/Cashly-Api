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
import java.util.Set;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM UserEntity u WHERE u.id = :id AND u.active = true")
    boolean canAuthenticate(@Param("id") UUID id);

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

    @Query("""
        SELECT u.id FROM UserEntity u WHERE u.id IN :ids AND (
          EXISTS (SELECT 1 FROM OrderEntity o WHERE o.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM InvoiceEntity i WHERE i.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM QuoteJpaEntity q WHERE q.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM PaymentEntity p WHERE p.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM ReturnEntity r WHERE r.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM CreditNoteEntity cn WHERE cn.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM DebitNoteEntity dn WHERE dn.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM BalanceTransactionEntity bt WHERE bt.createdBy = u.id)
          OR EXISTS (SELECT 1 FROM PurchaseEntity pch WHERE pch.createdBy = u.id)
        )
        """)
    Set<UUID> findIdsWithAssociatedRecords(@Param("ids") Set<UUID> ids);
}
