package com.cashly.cashly_api.returns.application.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InvoiceReturnDetailsQuery {
    List<ReturnDetails> findByInvoiceId(UUID invoiceId);

    record ReturnDetails(UUID id, String number, String type, String reason, BigDecimal total,
                        LocalDateTime createdAt, String createdByName, List<Line> lines) {}
    record Line(UUID productId, BigDecimal quantity, BigDecimal unitPrice,
                BigDecimal subtotal, List<Allocation> allocations) {}
    record Allocation(UUID warehouseId, BigDecimal allocated) {}
}
