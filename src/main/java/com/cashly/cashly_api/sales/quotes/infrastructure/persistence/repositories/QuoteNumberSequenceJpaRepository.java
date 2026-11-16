package com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories;

import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities.QuoteNumberSequenceJpaEntity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuoteNumberSequenceJpaRepository
        extends
            JpaRepository<QuoteNumberSequenceJpaEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM QuoteNumberSequenceJpaEntity s WHERE s.year = :year")
    Optional<QuoteNumberSequenceJpaEntity> findByYearForUpdate(
            @Param("year") int year);
}
