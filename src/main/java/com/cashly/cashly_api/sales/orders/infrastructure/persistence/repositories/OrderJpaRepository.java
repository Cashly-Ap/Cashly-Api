package com.cashly.cashly_api.sales.orders.infrastructure.persistence.repositories;

import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderEntity;
import com.cashly.cashly_api.sales.orders.infrastructure.persistence.entities.OrderLineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    default List<OrderEntity> findAllWithFilters(LocalDateTime dateFrom,
            LocalDateTime dateTo, UUID customerId, String status,
            UUID productId, UUID categoryId) {
        return findAllWithFilters(dateFrom, dateTo, customerId, status,
                productId, categoryId, java.util.Set.of(new UUID(0, 0)));
    }

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            LEFT JOIN FETCH o.lines l LEFT JOIN FETCH l.categorySnapshot
            WHERE o.id = :id
            """)
    Optional<OrderEntity> findWithLinesById(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT l FROM OrderLineEntity l
            LEFT JOIN FETCH l.allocations
            WHERE l.order.id = :orderId
            """)
    List<OrderLineEntity> findWithAllocationsForOrder(
            @Param("orderId") UUID orderId);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            LEFT JOIN FETCH o.lines l LEFT JOIN FETCH l.categorySnapshot
            WHERE (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            AND (:customerId IS NULL OR o.customerId = :customerId)
            AND (:status = 'ALL' OR o.status = :status OR (:status IS NULL AND o.status = 'DRAFT'))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol WHERE ol.order = o AND ol.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol2 LEFT JOIN ol2.categorySnapshot categorySnapshot
                WHERE ol2.order = o AND
                (categorySnapshot.categoryId = :categoryId OR ol2.productId IN :currentCategoryProductIds)
            ))
            """)
    List<OrderEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status, @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId,
            @Param("currentCategoryProductIds") java.util.Set<UUID> currentCategoryProductIds);

    @Query(value = """
            SELECT o FROM OrderEntity o
            WHERE (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            AND (:customerId IS NULL OR o.customerId = :customerId)
            AND (:status = 'ALL' OR o.status = :status OR (:status IS NULL AND o.status = 'DRAFT'))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol WHERE ol.order = o AND ol.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol2 LEFT JOIN ol2.categorySnapshot categorySnapshot
                WHERE ol2.order = o AND
                (categorySnapshot.categoryId = :categoryId OR ol2.productId IN :currentCategoryProductIds)
            ))
            AND (:search IS NULL OR :search = '' OR LOWER(o.orderNumber) LIKE :search
                OR LOWER(o.customerName) LIKE :search)
            """, countQuery = """
            SELECT COUNT(o) FROM OrderEntity o
            WHERE (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            AND (:customerId IS NULL OR o.customerId = :customerId)
            AND (:status = 'ALL' OR o.status = :status OR (:status IS NULL AND o.status = 'DRAFT'))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol WHERE ol.order = o AND ol.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM OrderLineEntity ol2 LEFT JOIN ol2.categorySnapshot categorySnapshot
                WHERE ol2.order = o AND
                (categorySnapshot.categoryId = :categoryId OR ol2.productId IN :currentCategoryProductIds)
            ))
            AND (:search IS NULL OR :search = '' OR LOWER(o.orderNumber) LIKE :search
                OR LOWER(o.customerName) LIKE :search)
            """)
    Page<OrderEntity> findPageWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("customerId") UUID customerId,
            @Param("status") String status, @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId,
            @Param("currentCategoryProductIds") java.util.Set<UUID> currentCategoryProductIds,
            @Param("search") String search, Pageable pageable);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            LEFT JOIN FETCH o.lines l LEFT JOIN FETCH l.categorySnapshot
            WHERE o.id IN :ids
            """)
    List<OrderEntity> findAllWithLinesByIds(
            @Param("ids") Collection<UUID> ids);

}
