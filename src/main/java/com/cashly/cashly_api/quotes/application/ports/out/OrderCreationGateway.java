package com.cashly.cashly_api.quotes.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface OrderCreationGateway {
    CreatedOrder create(Command command);
    record Command(UUID customerId, String customerName, String customerRif, UUID actorId, String actorName,
                   List<Line> lines) {
        public Command { lines = List.copyOf(lines); }
    }
    record Line(UUID productId, String productName, String productCode, UUID categoryId, BigDecimal quantity,
                BigDecimal unitPrice, List<Allocation> allocations) {
        public Line(UUID productId, String productName, String productCode, BigDecimal quantity,
                    BigDecimal unitPrice, List<Allocation> allocations) {
            this(productId, productName, productCode, null, quantity, unitPrice, allocations);
        }
        public Line { allocations = List.copyOf(allocations); }
    }
    record Allocation(UUID warehouseId, UUID productId, BigDecimal quantity) {}
    record CreatedOrder(UUID id, String number) {}
}
