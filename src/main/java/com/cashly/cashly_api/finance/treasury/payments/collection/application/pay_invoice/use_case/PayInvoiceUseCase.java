package com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_invoice.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.CashRegisterGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentCustomerGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.ports.out.PaymentInvoiceGateway;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.pay_invoice.request.PayInvoiceRequest;
import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResultResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.PaymentAllocation;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentAllocationRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PayInvoiceUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final PaymentInvoiceGateway invoices;
    private final PaymentCustomerGateway customers;
    private final CashRegisterGateway cashRegister;

    public PayInvoiceUseCase(PaymentRepository paymentRepository,
                              PaymentAllocationRepository allocationRepository,
                              PaymentInvoiceGateway invoices,
                              PaymentCustomerGateway customers,
                              CashRegisterGateway cashRegister) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoices = invoices;
        this.customers = customers;
        this.cashRegister = cashRegister;
    }

    @Transactional
    public PaymentResultResponse execute(UUID invoiceId, PayInvoiceRequest request,
                                          UUID authenticatedUserId,
                                          String authenticatedUserName) {
        PaymentInvoiceGateway.InvoiceSnapshot invoice = invoices.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal amount = request.amount();
        if (amount == null) {
            amount = invoice.total();
        }

        BigDecimal balanceUsed = BigDecimal.ZERO;
        if (request.useBalance() != null && request.useBalance() && request.balanceAmount() != null) {
            balanceUsed = request.balanceAmount();
        }

        BigDecimal paymentAmount = amount.subtract(balanceUsed);

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0
                && balanceUsed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0 && request.paymentMethodId() == null) {
            throw new IllegalArgumentException("Payment method is required when payment amount is greater than zero");
        }
        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            cashRegister.requireOpenRegister();
        }

        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            customers.consumeInvoiceBalance(new PaymentCustomerGateway.BalanceConsumption(
                    invoice.customerId(), balanceUsed, invoiceId, invoice.number(), authenticatedUserId));
        }

        int year = LocalDate.now().getYear();

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            int sequence = paymentRepository.getNextSequenceForYear(year);
            PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

            Payment payment = Payment.create(
                    paymentNumber,
                    invoice.customerId(),
                    paymentAmount,
                    request.paymentMethodId(),
                    request.referenceNumber(),
                    request.paymentDate(),
                    "Pago de factura " + invoice.number(),
                    authenticatedUserId,
                    authenticatedUserName,
                    PaymentStatus.APPLIED
            );

            Payment saved = paymentRepository.save(payment);
            cashRegister.recordMovement(new CashRegisterGateway.Movement(CashRegisterGateway.Direction.IN,
                    CashRegisterGateway.Type.PAYMENT, new CashRegisterGateway.PaymentEntry(saved.getAmount(),
                    saved.getPaymentMethodId(), saved.getNotes(), saved.getId().value(), authenticatedUserId,
                    authenticatedUserName)));

            PaymentAllocation allocation = PaymentAllocation.create(
                    saved.getId().value(),
                    invoiceId,
                    paymentAmount
            );
            allocationRepository.save(allocation);

            payment.apply(paymentAmount);
            paymentRepository.save(payment);
        }

        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            int balanceSequence = paymentRepository.getNextSequenceForYear(year);
            PaymentNumber balancePaymentNumber = PaymentNumber.of(year, balanceSequence);

            Payment balancePayment = Payment.create(
                    balancePaymentNumber,
                    invoice.customerId(),
                    balanceUsed,
                    null,
                    PaymentSource.CUSTOMER_BALANCE,
                    null,
                    request.paymentDate(),
                    "Pago con saldo a favor - Factura " + invoice.number(),
                    authenticatedUserId,
                    authenticatedUserName,
                    PaymentStatus.APPLIED
            );

            Payment savedBalancePayment = paymentRepository.save(balancePayment);

            PaymentAllocation balanceAllocation = PaymentAllocation.create(
                    savedBalancePayment.getId().value(),
                    invoiceId,
                    balanceUsed
            );
            allocationRepository.save(balanceAllocation);

            balancePayment.apply(balanceUsed);
            paymentRepository.save(balancePayment);
        }

        invoices.recalculateStatus(invoiceId);

        return new PaymentResultResponse(
                null,
                PaymentStatus.APPLIED.name(),
                "Pago registrado exitosamente",
                true
        );
    }
}
