package com.parsun.parsun_api.quotes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface QuoteJpaRepository extends JpaRepository<QuoteJpaEntity, UUID> {

    boolean existsByQuoteNumber(String quoteNumber);

    @Query("SELECT COALESCE(MAX(q.nextValue), 1) FROM QuoteNumberSequenceJpaEntity q WHERE q.year = :year")
    int findNextSequenceValueForYear(@Param("year") int year);
}
