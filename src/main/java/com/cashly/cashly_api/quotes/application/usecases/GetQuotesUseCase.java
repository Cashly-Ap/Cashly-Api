package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetQuotesUseCase {

    private final QuoteRepository quoteRepository;

    public GetQuotesUseCase(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public List<QuoteListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                            UUID customerId, String status,
                                            UUID productId, UUID categoryId,
                                            String orderBy, String orderDir) {
        return quoteRepository.findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId, orderBy, orderDir)
                .stream()
                .map(QuoteMapper::toListResponse)
                .toList();
    }
}
