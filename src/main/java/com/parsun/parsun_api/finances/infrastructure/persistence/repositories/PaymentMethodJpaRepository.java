package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodEntity, UUID> {

    Optional<PaymentMethodEntity> findByCode(String code);

    @Query("SELECT pm FROM PaymentMethodEntity pm WHERE (:activeOnly = false OR pm.isActive = true)")
    List<PaymentMethodEntity> findAllActive(@Param("activeOnly") boolean activeOnly);

    @Query("SELECT MAX(CAST(SUBSTRING(pm.code, 4) AS integer)) FROM PaymentMethodEntity pm WHERE pm.code LIKE 'PM-%'")
    Optional<Integer> findMaxCodeNumber();
}
