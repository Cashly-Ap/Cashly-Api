package com.cashly.cashly_api.quotes.application.products;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteCustomerGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteProductResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetProductsForQuoteUseCase {

    private final QuoteCustomerGateway customers;
    private final QuoteProductCatalogGateway products;

    public GetProductsForQuoteUseCase(QuoteCustomerGateway customers, QuoteProductCatalogGateway products) {
        this.customers = customers;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuoteProductResponse> execute(UUID customerId, String search,
                                                       UUID categoryId, Boolean composite,
                                                       int page, int size) {
        customers.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        var result = products.findForCustomer(customerId, search, categoryId, composite, page, size);
        var content = result.content().stream().map(product -> new QuoteProductResponse(product.id().toString(),
                product.code(), product.name(), product.categoryId() == null ? null : product.categoryId().toString(),
                product.categoryName(), product.composite(), product.active(), product.photo(), product.customerPrice()))
                .toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty());
    }
}
