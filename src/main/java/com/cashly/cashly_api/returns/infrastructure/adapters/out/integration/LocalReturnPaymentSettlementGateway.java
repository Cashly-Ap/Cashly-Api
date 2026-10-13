package com.cashly.cashly_api.returns.infrastructure.adapters.out.integration;
import com.cashly.cashly_api.returns.application.ports.out.ReturnPaymentSettlementGateway;
import com.cashly.cashly_api.returns.domain.entities.CancelledAllocation;
import com.cashly.cashly_api.payments.application.ports.in.ReturnPaymentOperations;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Component
public class LocalReturnPaymentSettlementGateway implements ReturnPaymentSettlementGateway {
    private final ReturnPaymentOperations operations;
    public LocalReturnPaymentSettlementGateway(ReturnPaymentOperations operations) { this.operations = operations; }
    public BigDecimal allocatedAmount(UUID invoiceId) { return operations.allocatedAmount(invoiceId); }
    public List<CancelledAllocation> capture(UUID invoiceId) {
        return operations.capture(invoiceId).stream().map(snapshot -> CancelledAllocation.create(snapshot.paymentId(), snapshot.invoiceId(), snapshot.amount())).toList();
    }
    public void cancelInvoicePayments(UUID invoiceId) { operations.cancelInvoicePayments(invoiceId); }
    public void restore(List<CancelledAllocation> allocations) {
        operations.restore(allocations.stream().map(allocation -> new ReturnPaymentOperations.AllocationSnapshot(
                allocation.getPaymentId(), allocation.getInvoiceId(), allocation.getAmount())).toList());
    }
}
