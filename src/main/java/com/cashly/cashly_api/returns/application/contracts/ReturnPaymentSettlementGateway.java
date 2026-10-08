package com.cashly.cashly_api.returns.application.contracts;
import com.cashly.cashly_api.returns.domain.entities.CancelledAllocation;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface ReturnPaymentSettlementGateway {
    BigDecimal allocatedAmount(UUID invoiceId);
    List<CancelledAllocation> capture(UUID invoiceId);
    void cancelInvoicePayments(UUID invoiceId);
    void restore(List<CancelledAllocation> allocations);
}
