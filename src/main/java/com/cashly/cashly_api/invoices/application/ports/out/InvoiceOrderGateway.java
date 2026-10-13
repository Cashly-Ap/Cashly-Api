package com.cashly.cashly_api.invoices.application.ports.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InvoiceOrderGateway {
    Order findOrder(UUID orderId);
    void revertInvoiceLink(UUID orderId);

    record Order(UUID id, String number, UUID customerId, String customerName, String customerRif,
                 String status, List<Line> lines) {
        public Order {
            lines = List.copyOf(lines);
        }
    }

    record Line(UUID productId, String productName, String productCode, BigDecimal quantity,
                BigDecimal unitPrice, List<Allocation> allocations) {
        public Line {
            allocations = List.copyOf(allocations);
        }
    }

    record Allocation(UUID warehouseId, BigDecimal quantity) {}
}
