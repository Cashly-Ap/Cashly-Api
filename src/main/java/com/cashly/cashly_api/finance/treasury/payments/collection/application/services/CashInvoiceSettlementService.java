package com.cashly.cashly_api.finance.treasury.payments.collection.application.services;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.in.CashInvoiceSettlement;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
public class CashInvoiceSettlementService implements CashInvoiceSettlement {
    private final PaymentRepository payments;
    private final PaymentAllocationRepository allocations;
    private final CashRegisterGateway cashRegister;
    public CashInvoiceSettlementService(PaymentRepository payments,
            PaymentAllocationRepository allocations,
            CashRegisterGateway cashRegister) {
        this.payments = payments;
        this.allocations = allocations;
        this.cashRegister = cashRegister;
    }
    @Transactional
    public void settle(Command command) {
        int year = LocalDate.now().getYear();
        int sequence = payments.getNextSequenceForYear(year);
        var payment = Payment.create(PaymentNumber.of(year, sequence),
                command.customerId(), command.amount(),
                command.paymentMethodId(), null, LocalDate.now(),
                "Pago de factura " + command.invoiceNumber(), command.actorId(),
                command.actorName(), PaymentStatus.APPLIED);
        var savedPayment = payments.save(payment);
        cashRegister.recordPayment(new CashRegisterGateway.PaymentEntry(
                savedPayment.getAmount(), savedPayment.getPaymentMethodId(),
                savedPayment.getNotes(), savedPayment.getId().value(),
                command.actorId(), command.actorName()));
        allocations.save(PaymentAllocation.create(savedPayment.getId().value(),
                command.invoiceId(), command.amount()));
        payment.apply(command.amount());
        payments.save(payment);
    }
}
