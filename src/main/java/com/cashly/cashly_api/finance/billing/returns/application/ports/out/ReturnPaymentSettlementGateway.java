package com.cashly.cashly_api.finance.billing.returns.application.ports.out;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.CancelledAllocation;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public interface ReturnPaymentSettlementGateway {
    BigDecimal allocatedAmount(UUID invoiceId);
    List<CancelledAllocation> capture(UUID invoiceId);
    void cancelInvoicePayments(UUID invoiceId);
    void restore(List<CancelledAllocation> allocations);
}
