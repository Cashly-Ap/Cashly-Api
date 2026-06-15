package com.parsun.parsun_api.cashregister.infrastructure.persistence;

import com.parsun.parsun_api.cashregister.infrastructure.persistence.jpa.CashRegisterJpaEntity;
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

    @Query("SELECT c FROM CashRegisterJpaEntity c WHERE c.status = 'OPEN' ORDER BY c.openedAt DESC")
    Optional<CashRegisterJpaEntity> findActiveClosure();

    @Query("SELECT c FROM CashRegisterJpaEntity c WHERE c.status = :status ORDER BY c.openedAt DESC")
    Optional<CashRegisterJpaEntity> findLatestByStatus(@Param("status") String status);
}
