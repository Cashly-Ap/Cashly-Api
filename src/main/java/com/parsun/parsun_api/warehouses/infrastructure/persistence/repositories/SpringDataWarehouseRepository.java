package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataWarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query("""
        SELECT w FROM WarehouseEntity w
        WHERE (:search IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR w.active = :active)
        """)
    Page<WarehouseEntity> findAllWithFilters(@Param("search") String search,
                                              @Param("active") Boolean active,
                                              Pageable pageable);
}
