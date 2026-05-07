package com.parsun.parsun_api.orders.infrastructure.persistence.repositories;

import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = {"lines"})
    Optional<OrderEntity> findWithLinesById(UUID id);

    @Query("""
            SELECT o FROM OrderEntity o LEFT JOIN FETCH o.lines
            WHERE (:customerId IS NULL OR o.customerId = :customerId)
            AND (:status IS NULL OR o.status = :status)
            AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
            AND (:toDate IS NULL OR o.createdAt <= :toDate)
            AND (:createdBy IS NULL OR o.createdBy = :createdBy)
            ORDER BY o.createdAt DESC
            """)
    List<OrderEntity> findAllWithFilters(
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("createdBy") UUID createdBy
    );

}
