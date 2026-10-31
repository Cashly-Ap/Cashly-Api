package com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.repositories;

import com.cashly.cashly_api.procurement.purchases.infrastructure.persistence.entities.PurchaseNumberSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseNumberSequenceJpaRepository
        extends
            JpaRepository<PurchaseNumberSequenceEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM PurchaseNumberSequenceEntity s WHERE s.year = :year")
    Optional<PurchaseNumberSequenceEntity> findByYearForUpdate(
            @Param("year") int year);
}
