package com.cashly.cashly_api.quotes.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.orders.application.ports.in.QuoteOrderCreationApi;
import com.cashly.cashly_api.quotes.application.ports.out.OrderCreationGateway;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class LocalOrderCreationGatewayTest {

    @Test
    void shouldPreserveCategorySnapshotWhenCreatingOrder() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        AtomicReference<QuoteOrderCreationApi.Command> received = new AtomicReference<>();
        QuoteOrderCreationApi api = command -> {
            received.set(command);
            return new QuoteOrderCreationApi.CreatedOrder(UUID.randomUUID(), "ORD-1");
        };
        var gateway = new LocalOrderCreationGateway(api);
        var line = new OrderCreationGateway.Line(productId, "Product", "P-1", categoryId,
                BigDecimal.ONE, BigDecimal.TEN,
                List.of(new OrderCreationGateway.Allocation(warehouseId, productId, BigDecimal.ONE)));

        gateway.create(new OrderCreationGateway.Command(customerId, "Customer", "RIF", null, null,
                List.of(line)));

        assertThat(received.get().lines()).singleElement()
                .extracting(QuoteOrderCreationApi.Line::categoryId)
                .isEqualTo(categoryId);
    }
}
