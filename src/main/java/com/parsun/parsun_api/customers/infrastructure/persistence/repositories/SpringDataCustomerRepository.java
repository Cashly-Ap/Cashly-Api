package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByRif(String rif);

    @Query("SELECT MAX(c.code) FROM CustomerEntity c")
    Optional<Long> findMaxCode();

    @Query("SELECT c.active FROM CustomerEntity c WHERE c.id = :id")
    Optional<Boolean> isActiveById(@Param("id") UUID id);

    @Query("SELECT COUNT(c) > 0 FROM CustomerEntity c WHERE c.id = :id AND c.active = true AND c.portalAccess = true")
    boolean isActivePortalAccessById(@Param("id") UUID id);

    @Query("""
        SELECT c FROM CustomerEntity c
        WHERE (:search IS NULL OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.rif) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:rif IS NULL OR LOWER(c.rif) LIKE LOWER(CONCAT('%', :rif, '%')))
        AND (:type IS NULL OR c.type = :type)
        AND (:active IS NULL OR c.active = :active)
        AND (:portalAccess IS NULL OR c.portalAccess = :portalAccess)
        AND (:state IS NULL OR EXISTS (
            SELECT 1 FROM CustomerAddressEntity ca WHERE ca.customerId = c.id AND LOWER(ca.state) LIKE LOWER(CONCAT('%', :state, '%'))
        ))
        AND (:municipality IS NULL OR EXISTS (
            SELECT 1 FROM CustomerAddressEntity ca WHERE ca.customerId = c.id AND LOWER(ca.municipality) LIKE LOWER(CONCAT('%', :municipality, '%'))
        ))
        AND (:postalCode IS NULL OR EXISTS (
            SELECT 1 FROM CustomerAddressEntity ca WHERE ca.customerId = c.id AND ca.postalCode = :postalCode
        ))
        AND (:hasActiveWarehouse IS NULL OR EXISTS (
            SELECT 1 FROM CustomerWarehouseAccessEntity cwa
            JOIN WarehouseEntity w ON w.id = cwa.warehouseId
            WHERE cwa.customerId = c.id AND w.active = true
        ))
        """)
    Page<CustomerEntity> findAllWithFilters(
        @Param("search") String search,
        @Param("rif") String rif,
        @Param("type") String type,
        @Param("active") Boolean active,
        @Param("portalAccess") Boolean portalAccess,
        @Param("state") String state,
        @Param("municipality") String municipality,
        @Param("postalCode") String postalCode,
        @Param("hasActiveWarehouse") Boolean hasActiveWarehouse,
        Pageable pageable
    );

    @Query("""
        SELECT c.id FROM CustomerEntity c WHERE c.id IN :ids AND (
          EXISTS (SELECT 1 FROM OrderEntity o WHERE o.customerId = c.id)
          OR EXISTS (SELECT 1 FROM InvoiceEntity i WHERE i.customerId = c.id)
          OR EXISTS (SELECT 1 FROM QuoteJpaEntity q WHERE q.customerId = c.id)
          OR EXISTS (SELECT 1 FROM PaymentEntity p WHERE p.customerId = c.id)
          OR EXISTS (SELECT 1 FROM ReturnEntity r WHERE r.customerId = c.id)
          OR EXISTS (SELECT 1 FROM CreditNoteEntity cn WHERE cn.customerId = c.id)
          OR EXISTS (SELECT 1 FROM DebitNoteEntity dn WHERE dn.customerId = c.id)
          OR EXISTS (SELECT 1 FROM BalanceTransactionEntity bt WHERE bt.customerId = c.id)
        )
        """)
    Set<UUID> findIdsWithAssociatedRecords(@Param("ids") Set<UUID> ids);
}

