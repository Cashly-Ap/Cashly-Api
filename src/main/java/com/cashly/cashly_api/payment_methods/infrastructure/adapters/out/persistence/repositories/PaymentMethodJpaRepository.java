package com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.payment_methods.infrastructure.adapters.out.persistence.entities.PaymentMethodEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodEntity, UUID> {

    Optional<PaymentMethodEntity> findByCode(String code);

    @Query("SELECT pm FROM PaymentMethodEntity pm WHERE (:active IS NULL OR pm.isActive = :active)")
    List<PaymentMethodEntity> findAllActive(@Param("active") Boolean active);

    @Query("SELECT MAX(CAST(SUBSTRING(pm.code, 4) AS integer)) FROM PaymentMethodEntity pm WHERE pm.code LIKE 'PM-%'")
    Optional<Integer> findMaxCodeNumber();

    @Query("""
        SELECT pm FROM PaymentMethodEntity pm
        WHERE (:search IS NULL OR LOWER(pm.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(pm.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:active IS NULL OR pm.isActive = :active)
        """)
    Page<PaymentMethodEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable);
}
