package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.products.domain.valueobjects.ProductPrice;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteProductResponse;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductsForQuoteUseCase Unit Tests")
class GetProductsForQuoteUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private GetProductsForQuoteUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetProductsForQuoteUseCase(customerRepository, productRepository, categoryRepository);
    }

    @Test
    @DisplayName("should return products with customer price from price_type")
    void shouldReturnProductsWithCustomerPrice() {
        UUID priceTypeId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId, priceTypeId);
        Product product = buildProduct(UUID.randomUUID(), "PROD-001", "Product A",
                categoryId, BigDecimal.TEN, priceTypeId, new BigDecimal("7.50"));

        PageResponse<Product> productsPage = new PageResponse<>(
                List.of(product), 1L, 1, 20, 0, true, true, false
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(productRepository.findAllWithFilters(any(), any(), any(), any(), anyInt(), anyInt(), eq("name"), eq("asc")))
                .thenReturn(productsPage);
        when(categoryRepository.findById(categoryId)).thenReturn(
                Optional.of(com.parsun.parsun_api.categories.domain.entities.Category.reconstitute(
                        categoryId, "Test Category", null, true, LocalDateTime.now(), LocalDateTime.now()))
        );

        PageResponse<QuoteProductResponse> result = useCase.execute(
                customerId, null, null, null, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        QuoteProductResponse resp = result.content().get(0);
        assertEquals("PROD-001", resp.code());
        assertEquals("Product A", resp.name());
        assertEquals("Test Category", resp.categoryName());
        assertEquals(0, new BigDecimal("7.50").compareTo(resp.customerPrice()));

        verify(customerRepository).findById(new CustomerId(customerId));
        verify(productRepository).findAllWithFilters(any(), any(), any(), any(), anyInt(), anyInt(), eq("name"), eq("asc"));
    }

    @Test
    @DisplayName("should fall back to base price when price_type is null")
    void shouldFallbackToBasePriceWhenPriceTypeIsNull() {
        UUID categoryId = UUID.randomUUID();

        Customer customer = buildCustomer(customerId, null);
        Product product = buildProduct(UUID.randomUUID(), "PROD-002", "Product B",
                categoryId, new BigDecimal("10.00"), null, null);

        PageResponse<Product> productsPage = new PageResponse<>(
                List.of(product), 1L, 1, 20, 0, true, true, false
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(productRepository.findAllWithFilters(any(), any(), any(), any(), anyInt(), anyInt(), eq("name"), eq("asc")))
                .thenReturn(productsPage);
        when(categoryRepository.findById(categoryId)).thenReturn(
                Optional.of(com.parsun.parsun_api.categories.domain.entities.Category.reconstitute(
                        categoryId, "Cat B", null, true, LocalDateTime.now(), LocalDateTime.now()))
        );

        PageResponse<QuoteProductResponse> result = useCase.execute(
                customerId, null, null, null, 0, 20);

        assertEquals(0, new BigDecimal("10.00").compareTo(result.content().get(0).customerPrice()));
    }

    @Test
    @DisplayName("should return empty page when no products match")
    void shouldReturnEmptyPageWhenNoProductsMatch() {
        Customer customer = buildCustomer(customerId, null);
        PageResponse<Product> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 20, 0, true, true, true
        );

        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(customer));
        when(productRepository.findAllWithFilters(any(), any(), any(), any(), anyInt(), anyInt(), eq("name"), eq("asc")))
                .thenReturn(emptyPage);

        PageResponse<QuoteProductResponse> result = useCase.execute(
                customerId, null, null, null, 0, 20);

        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
    }

    @Test
    @DisplayName("should throw when customer is not found")
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(customerId, null, null, null, 0, 20));

        verify(productRepository, never()).findAllWithFilters(any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    // --- Helpers ---

    private Customer buildCustomer(UUID customerId, UUID priceTypeId) {
        return Customer.reconstitute(
                new CustomerId(customerId),
                new CustomerCode(1001L),
                new LegalName("Empresa Test C.A."),
                CustomerType.BUSINESS,
                new Rif("J-12345678-9"),
                new CustomerAlias("Test"),
                new CreditDays(30),
                new CreditLimit(BigDecimal.ZERO),
                true, LocalDateTime.now(), LocalDateTime.now(),
                List.of(), List.of(), List.of(),
                false, null, null, priceTypeId
        );
    }

    private Product buildProduct(UUID productId, String code, String name, UUID categoryId,
                                  BigDecimal basePrice, UUID priceTypeId, BigDecimal customerPrice) {
        List<ProductPrice> productPrices = priceTypeId != null && customerPrice != null
                ? List.of(ProductPrice.reconstitute(UUID.randomUUID(), priceTypeId, customerPrice))
                : List.of();

        return Product.reconstitute(
                productId, code, name, null, categoryId, false, true, null,
                basePrice, BigDecimal.TEN, BigDecimal.valueOf(0.25),
                List.of(), productPrices, LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
