package com.parsun.parsun_api.orders.infrastructure.persistence.repositories;

import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderEntity;
import com.parsun.parsun_api.orders.infrastructure.persistence.entities.OrderLineEntity;
import com.parsun.parsun_api.products.infrastructure.persistence.entities.ProductEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            LEFT JOIN FETCH o.lines
            WHERE o.id = :id
            """)
    Optional<OrderEntity> findWithLinesById(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT l FROM OrderLineEntity l
            LEFT JOIN FETCH l.allocations
            WHERE l.order.id = :orderId
            """)
    List<OrderLineEntity> findWithAllocationsForOrder(@Param("orderId") UUID orderId);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            LEFT JOIN FETCH o.lines l
            WHERE (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            AND (:customerId IS NULL OR o.customerId = :customerId)
            AND ((:status IS NULL AND o.status = 'DRAFT') OR (:status IS NOT NULL AND o.status = :status))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol WHERE ol.order = o AND ol.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol2 WHERE ol2.order = o AND ol2.productId IN (
                    SELECT p.id FROM ProductEntity p WHERE p.categoryId = :categoryId
                )
            ))
            """)
    List<OrderEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId,
            Sort sort
    );

}
