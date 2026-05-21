package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnJpaRepository extends JpaRepository<ReturnEntity, UUID> {

    @Query("""
            SELECT r FROM ReturnEntity r
            WHERE (:dateFrom IS NULL OR r.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR r.createdAt <= :dateTo)
            AND (:customerId IS NULL OR r.customerId = :customerId)
            AND (:returnType IS NULL OR r.returnType = :returnType)
            ORDER BY r.createdAt DESC
            """)
    List<ReturnEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("returnType") String returnType
    );

    Optional<ReturnEntity> findByInvoiceId(UUID invoiceId);
}
