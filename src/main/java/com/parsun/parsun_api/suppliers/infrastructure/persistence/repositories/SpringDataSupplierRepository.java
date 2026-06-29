package com.parsun.parsun_api.suppliers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.suppliers.infrastructure.persistence.entities.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataSupplierRepository extends JpaRepository<SupplierEntity, UUID> {

    @Query("SELECT MAX(s.code) FROM SupplierEntity s")
    Optional<Long> findMaxCode();

    @Query("""
        SELECT s FROM SupplierEntity s
        WHERE (:search IS NULL OR LOWER(s.legalName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.rif) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:type IS NULL OR s.type = :type)
        AND (:active IS NULL OR s.active = :active)
        AND (:state IS NULL OR EXISTS (
            SELECT 1 FROM SupplierAddressEntity sa WHERE sa.supplierId = s.id AND LOWER(sa.state) LIKE LOWER(CONCAT('%', :state, '%'))
        ))
        """)
    Page<SupplierEntity> findAllWithFilters(
        @Param("search") String search,
        @Param("type") String type,
        @Param("active") Boolean active,
        @Param("state") String state,
        Pageable pageable
    );
}
