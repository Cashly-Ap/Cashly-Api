package com.cashly.cashly_api.sales.quotes.application.list.use_case;

import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.sales.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;

import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetQuotesUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteProductCatalogGateway products;

    public GetQuotesUseCase(QuoteRepository quoteRepository,
            QuoteProductCatalogGateway products) {
        this.quoteRepository = quoteRepository;
        this.products = products;
    }

    public List<QuoteListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status, UUID productId, UUID categoryId,
            String orderBy, String orderDir) {
        var currentCategoryProductIds = categoryId == null
                ? java.util.Set.<UUID>of()
                : products.findProductIdsByCurrentCategory(categoryId);
        return quoteRepository
                .findAllWithFilters(dateFrom, dateTo, customerId, status,
                        productId, categoryId, currentCategoryProductIds,
                        orderBy, orderDir)
                .stream().map(QuoteMapper::toListResponse).toList();
    }
    public List<QuoteListResponse> execute(LocalDate dateFrom, LocalDate dateTo,
            UUID customerId, String status, UUID productId, UUID categoryId,
            String orderBy, String orderDir, DocumentAction action) {
        if (action == null)
            return execute(dateFrom, dateTo, customerId, status, productId,
                    categoryId, orderBy, orderDir);
        String eligibleStatus = switch (action) {
            case EDIT, CANCEL, CONVERT_TO_ORDER, CONVERT_TO_INVOICE,
                    DELETE -> "DRAFT";
            default -> throw new IllegalArgumentException(
                    "Unsupported quote action: " + action);
        };
        if (status != null && !status.isBlank() && !"ALL".equals(status)
                && !eligibleStatus.equals(status)) {
            return List.of();
        }
        if (action == DocumentAction.DELETE) {
            var currentCategoryProductIds = categoryId == null
                    ? java.util.Set.<UUID>of()
                    : products.findProductIdsByCurrentCategory(categoryId);
            return quoteRepository.findAllEligibleForDeletion(dateFrom, dateTo,
                    customerId, eligibleStatus, productId, categoryId,
                    currentCategoryProductIds, orderBy, orderDir).stream()
                    .map(QuoteMapper::toListResponse).toList();
        }
        return execute(dateFrom, dateTo, customerId, eligibleStatus, productId,
                categoryId, orderBy, orderDir);
    }

}
