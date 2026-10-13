package com.cashly.cashly_api.cash_register.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.cash_register.infrastructure.adapters.out.persistence.entities.CashRegisterJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterJpaRepository
        extends JpaRepository<CashRegisterJpaEntity, UUID>,
                JpaSpecificationExecutor<CashRegisterJpaEntity> {

    Optional<CashRegisterJpaEntity> findByClosureDate(LocalDate closureDate);

    @Query("SELECT c FROM CashRegisterJpaEntity c WHERE c.status = 'OPEN'")
    Optional<CashRegisterJpaEntity> findActiveClosure();

    @Query("SELECT c FROM CashRegisterJpaEntity c WHERE c.status = :status ORDER BY c.openedAt DESC")
    Optional<CashRegisterJpaEntity> findLatestByStatus(@Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM cash_register_details crd WHERE crd.payment_method_id = :paymentMethodId", nativeQuery = true)
    long countDetailsByPaymentMethodId(@Param("paymentMethodId") UUID paymentMethodId);
}
