package com.parsun.parsun_api.purchases.infrastructure.persistence.repositories;

import com.parsun.parsun_api.purchases.infrastructure.persistence.entities.PurchaseEntity;
import com.parsun.parsun_api.purchases.infrastructure.persistence.entities.PurchaseLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseJpaRepository extends JpaRepository<PurchaseEntity, UUID> {

    @Query("""
            SELECT DISTINCT p FROM PurchaseEntity p
            LEFT JOIN FETCH p.lines
            WHERE p.id = :id
            """)
    Optional<PurchaseEntity> findWithLinesById(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT l FROM PurchaseLineEntity l
            LEFT JOIN FETCH l.allocations
            WHERE l.purchase.id = :purchaseId
            """)
    List<PurchaseLineEntity> findWithAllocationsForPurchase(@Param("purchaseId") UUID purchaseId);

    @Query("""
            SELECT DISTINCT p FROM PurchaseEntity p
            LEFT JOIN FETCH p.lines l
            WHERE (:dateFrom IS NULL OR p.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR p.createdAt <= :dateTo)
            AND (:supplierId IS NULL OR p.supplierId = :supplierId)
            AND ((:status IS NULL AND p.status = 'DRAFT') OR (:status IS NOT NULL AND p.status = :status))
            AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM PurchaseLineEntity pl WHERE pl.purchase = p AND pl.productId = :productId
            ))
            AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM PurchaseLineEntity pl2 WHERE pl2.purchase = p AND pl2.productId IN (
                    SELECT pr.id FROM com.parsun.parsun_api.products.infrastructure.persistence.entities.ProductEntity pr WHERE pr.categoryId = :categoryId
                )
            ))
            """)
    List<PurchaseEntity> findAllWithFilters(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("supplierId") UUID supplierId,
            @Param("status") String status,
            @Param("productId") UUID productId,
            @Param("categoryId") UUID categoryId
    );
}
