package com.cashly.cashly_api.finance.treasury.payments.collection.application.services;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.ReturnPaymentOperations;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.services.PaymentAllocationService;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public class ReturnPaymentOperationsService implements ReturnPaymentOperations {
    private final PaymentRepository payments;
    private final PaymentAllocationRepository allocations;
    private final PaymentAllocationService allocationService;
    public ReturnPaymentOperationsService(PaymentRepository payments, PaymentAllocationRepository allocations, PaymentAllocationService allocationService) {
        this.payments = payments;
        this.allocations = allocations;
        this.allocationService = allocationService;
    }
    public BigDecimal allocatedAmount(UUID invoiceId) { return allocations.getAllocatedAmountByInvoiceId(invoiceId); }
    public List<AllocationSnapshot> capture(UUID invoiceId) {
        return allocations.findByInvoiceId(invoiceId).stream()
                .map(allocation -> new AllocationSnapshot(allocation.getPaymentId(), allocation.getInvoiceId(), allocation.getAllocatedAmount())).toList();
    }
    @Transactional
    public void cancelInvoicePayments(UUID invoiceId) {
        allocations.findByInvoiceId(invoiceId).stream().map(PaymentAllocation::getPaymentId).distinct()
                .forEach(id -> allocationService.cancelPayment(id, "Return created"));
    }
    @Transactional
    public void restore(List<AllocationSnapshot> snapshots) {
        for (AllocationSnapshot snapshot : snapshots) {
            var payment = payments.findById(PaymentId.of(snapshot.paymentId()))
                    .orElseThrow(() -> new IllegalStateException("Payment not found: " + snapshot.paymentId()));
            payment.reactivate();
            payments.save(payment);
            allocations.save(PaymentAllocation.create(snapshot.paymentId(), snapshot.invoiceId(), snapshot.amount()));
        }
    }
}
