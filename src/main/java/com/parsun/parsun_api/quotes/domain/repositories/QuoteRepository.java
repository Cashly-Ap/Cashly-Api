package com.parsun.parsun_api.quotes.domain.repositories;

import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository {
    Quote save(Quote quote);
    Optional<Quote> findById(UUID id);
    List<Quote> findAll();
    List<Quote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                   UUID customerId, String status,
                                   UUID productId, UUID categoryId,
                                   String orderBy, String orderDir);
    boolean existsByQuoteNumber(QuoteNumber quoteNumber);
    int getNextSequenceForYear(int year);
    void delete(UUID id);
}
