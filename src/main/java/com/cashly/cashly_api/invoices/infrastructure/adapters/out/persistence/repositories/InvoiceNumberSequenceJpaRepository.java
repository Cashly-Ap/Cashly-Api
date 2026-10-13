package com.cashly.cashly_api.invoices.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.invoices.infrastructure.adapters.out.persistence.entities.InvoiceNumberSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceNumberSequenceJpaRepository extends JpaRepository<InvoiceNumberSequenceEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceNumberSequenceEntity s WHERE s.year = :year")
    Optional<InvoiceNumberSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
