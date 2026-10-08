package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.contracts.QuoteCustomerGateway;
import com.cashly.cashly_api.quotes.application.contracts.QuoteProductCatalogGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductsForQuoteUseCaseTest {
    @Mock QuoteCustomerGateway customers;
    @Mock QuoteProductCatalogGateway products;
    private GetProductsForQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductsForQuoteUseCase(customers, products);
    }

    @Test
    void shouldReturnCustomerPricedProductsAndPageMetadata() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(Optional.of(
                new QuoteCustomerGateway.Customer(customerId, "Acme", "RIF", UUID.randomUUID())));
        when(products.findForCustomer(customerId, "search", categoryId, false, 2, 10)).thenReturn(
                new PageResponse<>(List.of(new QuoteProductCatalogGateway.ProductEntry(productId, "P-1", "Product",
                        categoryId, "Category", false, true, "photo", new BigDecimal("7.50"))),
                        1L, 1, 10, 2, false, true, false));

        var result = useCase.execute(customerId, "search", categoryId, false, 2, 10);

        assertThat(result.content()).singleElement().satisfies(product -> {
            assertThat(product.id()).isEqualTo(productId.toString());
            assertThat(product.categoryName()).isEqualTo("Category");
            assertThat(product.customerPrice()).isEqualByComparingTo("7.50");
        });
        assertThat(result.totalElements()).isEqualTo(1L);
        verify(products).findForCustomer(customerId, "search", categoryId, false, 2, 10);
    }

    @Test
    void shouldFailBeforeProductLookupWhenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(customerId, null, null, null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Customer not found");
        verifyNoInteractions(products);
    }
}
