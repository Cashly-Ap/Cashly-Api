package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteProductResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class GetProductsForQuoteUseCase {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public GetProductsForQuoteUseCase(CustomerRepository customerRepository,
                                       ProductRepository productRepository,
                                       CategoryRepository categoryRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<QuoteProductResponse> execute(UUID customerId, String search,
                                                       UUID categoryId, Boolean composite,
                                                       int page, int size) {
        Customer customer = customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        PageResponse<Product> productsPage = productRepository.findAllWithFilters(
                search, categoryId, true, composite, page, size, "name", "asc");

        var content = productsPage.content().stream()
                .map(product -> toResponse(product, customer))
                .toList();

        return new PageResponse<>(content, productsPage.totalElements(), productsPage.totalPages(),
                productsPage.size(), productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private QuoteProductResponse toResponse(Product product, Customer customer) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(c -> c.getName())
                .orElse("");

        BigDecimal customerPrice = resolveCustomerPrice(product, customer.getPriceTypeId());

        return new QuoteProductResponse(
                product.getId().toString(),
                product.getCode(),
                product.getName(),
                product.getCategoryId() != null ? product.getCategoryId().toString() : null,
                categoryName,
                product.isComposite(),
                product.isActive(),
                product.getPhoto(),
                customerPrice
        );
    }

    private BigDecimal resolveCustomerPrice(Product product, UUID priceTypeId) {
        if (priceTypeId == null) {
            return product.getPurchasePrice();
        }
        return product.getProductPrices().stream()
                .filter(pp -> priceTypeId.equals(pp.getPriceTypeId()))
                .map(pp -> pp.getPrice())
                .findFirst()
                .orElse(product.getPurchasePrice());
    }
}
