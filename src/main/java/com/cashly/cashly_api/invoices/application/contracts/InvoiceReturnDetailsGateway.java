package com.cashly.cashly_api.invoices.application.contracts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InvoiceReturnDetailsGateway {
    List<ReturnDetails> findByInvoiceId(UUID invoiceId);

    record ReturnDetails(UUID id, String number, String type, String reason, BigDecimal total,
                         LocalDateTime createdAt, String createdByName, List<Line> lines) {}
    record Line(UUID productId, String productName, String productCode, BigDecimal quantity,
                BigDecimal unitPrice, BigDecimal subtotal, List<Allocation> allocations) {}
    record Allocation(UUID warehouseId, String warehouseName, BigDecimal allocated) {}
}
