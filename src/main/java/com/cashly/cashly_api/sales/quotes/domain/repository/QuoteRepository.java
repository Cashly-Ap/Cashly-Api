package com.cashly.cashly_api.sales.quotes.domain.repository;

import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;

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
                                   UUID productId, UUID categoryId, java.util.Set<UUID> currentCategoryProductIds,
                                   String orderBy, String orderDir);
    default List<Quote> findAllWithFilters(LocalDate dateFrom, LocalDate dateTo,
                                           UUID customerId, String status, UUID productId, UUID categoryId,
                                           String orderBy, String orderDir) {
        return findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId,
                java.util.Set.of(), orderBy, orderDir);
    }
    boolean existsByQuoteNumber(QuoteNumber quoteNumber);
    int getNextSequenceForYear(int year);
    void delete(UUID id);
}
