package com.cashly.cashly_api.orders.application.usecases;

import com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

class GetOrdersCategoryQueryBoundaryTest {
    @Test
    void resolvesCurrentCategoryProductIdsThroughCatalogContract() {
        OrderRepository orders = mock(OrderRepository.class);
        OrderProductCatalogGateway catalog = mock(OrderProductCatalogGateway.class);
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(catalog.findProductIdsByCurrentCategory(categoryId)).thenReturn(Set.of(productId));

        new GetOrdersUseCase(orders, catalog).execute(null, null, null, null, null, categoryId, null, null);

        verify(orders).findAllWithFilters(null, null, null, null, null, categoryId, Set.of(productId), null, null);
    }
}
