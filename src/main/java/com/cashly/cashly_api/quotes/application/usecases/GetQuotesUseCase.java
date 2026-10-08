package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.application.contracts.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetQuotesUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteProductCatalogGateway products;

    public GetQuotesUseCase(QuoteRepository quoteRepository, QuoteProductCatalogGateway products) {
        this.quoteRepository = quoteRepository;
        this.products = products;
    }

    public List<QuoteListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
                                            UUID customerId, String status,
                                            UUID productId, UUID categoryId,
                                            String orderBy, String orderDir) {
        var currentCategoryProductIds = categoryId == null ? java.util.Set.<UUID>of()
                : products.findProductIdsByCurrentCategory(categoryId);
        return quoteRepository.findAllWithFilters(dateFrom, dateTo, customerId, status, productId, categoryId,
                        currentCategoryProductIds, orderBy, orderDir)
                .stream()
                .map(QuoteMapper::toListResponse)
                .toList();
    }
}
