package com.parsun.parsun_api.payments.application.usecases;

import com.parsun.parsun_api.cashregister.application.services.CashMovementService;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.parsun.parsun_api.cashregister.domain.valueobjects.CashMovementType;
import com.parsun.parsun_api.payments.application.dto.request.PayInvoiceRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentSource;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentStatus;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.services.InvoiceStatusService;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PayInvoiceUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;
    private final CashMovementService cashMovementService;

    public PayInvoiceUseCase(PaymentRepository paymentRepository,
                              PaymentAllocationRepository allocationRepository,
                              InvoiceRepository invoiceRepository,
                              InvoiceStatusService invoiceStatusService,
                              CreditNoteRepository creditNoteRepository,
                              DebitNoteRepository debitNoteRepository,
                              ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase,
                              CashMovementService cashMovementService) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.consumeCustomerBalanceUseCase = consumeCustomerBalanceUseCase;
        this.cashMovementService = cashMovementService;
    }

    @Transactional
    public PaymentResultResponse execute(UUID invoiceId, PayInvoiceRequest request,
                                          UUID authenticatedUserId,
                                          String authenticatedUserName) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal amount = request.amount();
        if (amount == null) {
            BigDecimal totalCreditNotes = creditNoteRepository.findByInvoiceId(invoiceId).stream()
                    .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDebitNotes = debitNoteRepository.findByInvoiceId(invoiceId).stream()
                    .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amount = invoice.calculateTotal(totalCreditNotes, totalDebitNotes);
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
            cashMovementService.requireOpenRegister();
        }

        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            consumeCustomerBalanceUseCase.execute(
                    invoice.getCustomerId(),
                    BalanceType.INVOICES,
                    balanceUsed,
                    BalanceType.INVOICES,
                    invoiceId,
                    invoice.getInvoiceNumber().value(),
                    null,
                    authenticatedUserId
            );
        }

        int year = LocalDate.now().getYear();

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            int sequence = paymentRepository.getNextSequenceForYear(year);
            PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

            Payment payment = Payment.create(
                    paymentNumber,
                    invoice.getCustomerId(),
                    paymentAmount,
                    request.paymentMethodId(),
                    request.referenceNumber(),
                    request.paymentDate(),
                    "Pago de factura " + invoice.getInvoiceNumber().value(),
                    authenticatedUserId,
                    authenticatedUserName,
                    PaymentStatus.APPLIED
            );

            Payment saved = paymentRepository.save(payment);
            cashMovementService.record(
                    CashMovementDirection.IN,
                    CashMovementType.PAYMENT,
                    saved.getAmount(),
                    saved.getPaymentMethodId(),
                    saved.getNotes(),
                    "PAYMENT",
                    saved.getId().value(),
                    authenticatedUserId,
                    authenticatedUserName
            );

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
                    invoice.getCustomerId(),
                    balanceUsed,
                    null,
                    PaymentSource.CUSTOMER_BALANCE,
                    null,
                    request.paymentDate(),
                    "Pago con saldo a favor - Factura " + invoice.getInvoiceNumber().value(),
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

        invoiceStatusService.recalculate(invoiceId);

        return new PaymentResultResponse(
                null,
                PaymentStatus.APPLIED.name(),
                "Pago registrado exitosamente",
                true
        );
    }
}
