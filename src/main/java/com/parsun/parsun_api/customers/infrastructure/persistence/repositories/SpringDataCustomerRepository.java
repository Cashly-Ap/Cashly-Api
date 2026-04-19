package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    @Query("SELECT MAX(c.code) FROM CustomerEntity c")
    Optional<Long> findMaxCode();

    @Query("""
        SELECT DISTINCT c FROM CustomerEntity c
        LEFT JOIN FETCH c.addresses a
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
        """)
    List<CustomerEntity> findAllWithFilters(
        @Param("search") String search,
        @Param("rif") String rif,
        @Param("type") String type,
        @Param("active") Boolean active,
        @Param("portalAccess") Boolean portalAccess,
        @Param("state") String state,
        @Param("municipality") String municipality,
        @Param("postalCode") String postalCode
    );
}
