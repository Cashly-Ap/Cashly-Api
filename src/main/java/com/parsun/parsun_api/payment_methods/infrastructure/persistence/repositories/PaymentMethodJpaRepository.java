package com.parsun.parsun_api.payment_methods.infrastructure.persistence.repositories;

import com.parsun.parsun_api.payment_methods.infrastructure.persistence.entities.PaymentMethodEntity;
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
}
