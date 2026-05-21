package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.DebitNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DebitNoteJpaRepository extends JpaRepository<DebitNoteEntity, UUID> {

    @Query("""
            SELECT d FROM DebitNoteEntity d
            WHERE (:dateFrom IS NULL OR d.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR d.createdAt <= :dateTo)
            AND (:customerId IS NULL OR d.customerId = :customerId)
            AND (:status IS NULL OR d.status = :status)
            ORDER BY d.createdAt DESC
            """)
    List<DebitNoteEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status
    );
}
