package com.cashly.cashly_api.sales.quotes.application.list.use_case;

import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.sales.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;

import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.dto.PageResponses;

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

    public PageResponse<QuoteListResponse> executePage(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, String status, UUID productId,
            UUID categoryId, String search, int page, int size, String orderBy,
            String orderDir, DocumentAction action) {
        if (action != null) {
            List<QuoteListResponse> all = execute(dateFrom, dateTo, customerId,
                    status, productId, categoryId, orderBy, orderDir, action)
                    .stream().filter(quote -> matchesSearch(search,
                            quote.quoteNumber(), quote.customerName(),
                            quote.customerRif()))
                    .toList();
            return PageResponses.page(all, page, size);
        }
        var currentCategoryProductIds = categoryId == null
                ? java.util.Set.<UUID>of()
                : products.findProductIdsByCurrentCategory(categoryId);
        PageResponse<Quote> quotes = quoteRepository.findPageWithFilters(
                dateFrom, dateTo, customerId, status, productId, categoryId,
                currentCategoryProductIds, search, page, size, orderBy,
                orderDir);
        List<QuoteListResponse> content = quotes.content().stream()
                .map(QuoteMapper::toListResponse).toList();
        return new PageResponse<>(content, quotes.totalElements(),
                quotes.totalPages(), quotes.size(), quotes.number(),
                quotes.first(), quotes.last(), content.isEmpty());
    }

    private static boolean matchesSearch(String search, String... values) {
        if (search == null || search.isBlank())
            return true;
        String term = search.trim().toLowerCase();
        return java.util.Arrays.stream(values)
                .filter(java.util.Objects::nonNull)
                .anyMatch(value -> value.toLowerCase().contains(term));
    }

}
