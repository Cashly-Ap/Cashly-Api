package com.cashly.cashly_api.sales.orders.application.products.use_case;

import com.cashly.cashly_api.sales.orders.application.ports.out.OrderCustomerGateway;
import com.cashly.cashly_api.sales.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.sales.orders.application.dto.response.OrderProductResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductsForOrderUseCaseUnitTest {
    @Mock
    private OrderCustomerGateway customers;
    @Mock
    private OrderProductCatalogGateway products;

    private GetProductsForOrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductsForOrderUseCase(customers, products);
    }

    @Test
    void preservesCatalogFiltersStockAndPaginationFromProviderContract() {
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(
                Optional.of(new OrderCustomerGateway.Customer(customerId,
                        "Customer", "RIF")));
        var stock = new OrderProductCatalogGateway.Stock(warehouseId, "WH-01",
                "Main", 12, 2, 10);
        var product = new OrderProductCatalogGateway.ProductEntry(productId,
                "SKU-1", "Product", categoryId, "Category", false, true, null,
                BigDecimal.TEN, 12, List.of(stock));
        when(products.findForCustomer(customerId, "prod", categoryId, false,
                true, 1, 5))
                .thenReturn(new PageResponse<>(List.of(product), 7, 2, 5, 1,
                        false, true, false));

        PageResponse<OrderProductResponse> result = useCase.execute(customerId,
                "prod", categoryId, false, true, 1, 5);

        assertEquals(7, result.totalElements());
        assertEquals(2, result.totalPages());
        assertEquals(1, result.number());
        assertEquals("SKU-1", result.content().getFirst().code());
        assertEquals(10,
                result.content().getFirst().stock().getFirst().available());
        verify(products).findForCustomer(customerId, "prod", categoryId, false,
                true, 1, 5);
    }

    @Test
    void rejectsUnknownCustomerBeforeRequestingCatalog() {
        UUID customerId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase
                .execute(customerId, null, null, null, null, 0, 20));

        verifyNoInteractions(products);
    }

    @Test
    void keepsEmptyPageMetadata() {
        UUID customerId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(
                Optional.of(new OrderCustomerGateway.Customer(customerId,
                        "Customer", "RIF")));
        when(products.findForCustomer(customerId, null, null, null, null, 0,
                20))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 20, 0, true,
                        true, true));

        var result = useCase.execute(customerId, null, null, null, null, 0, 20);

        assertTrue(result.empty());
        assertTrue(result.content().isEmpty());
    }
}
