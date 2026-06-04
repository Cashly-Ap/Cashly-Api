package com.parsun.parsun_api.advances.infrastructure.persistence;

import com.parsun.parsun_api.advances.infrastructure.persistence.entities.AdvanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdvanceJpaRepository extends JpaRepository<AdvanceEntity, UUID> {

    @Query("""
            SELECT a FROM AdvanceEntity a
            WHERE (:dateFrom IS NULL OR a.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR a.createdAt <= :dateTo)
            AND (:customerId IS NULL OR a.customerId = :customerId)
            AND (:status IS NULL OR a.status = :status)
            ORDER BY a.createdAt DESC
            """)
    List<AdvanceEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status
    );

    @Query("SELECT a FROM AdvanceEntity a WHERE a.customerId = :customerId AND a.status IN ('ACTIVE', 'PARTIALLY_CONSUMED')")
    List<AdvanceEntity> findActiveByCustomerId(@Param("customerId") UUID customerId);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(a.advance_number, 9) AS SIGNED)) FROM advances a WHERE a.advance_number LIKE :yearPattern", nativeQuery = true)
    Optional<Integer> findMaxSequenceNumberForYear(@Param("yearPattern") String yearPattern);
}
