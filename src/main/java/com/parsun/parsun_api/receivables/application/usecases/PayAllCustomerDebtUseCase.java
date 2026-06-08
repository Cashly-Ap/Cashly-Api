package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.request.PayCustomerDebtRequest;
import com.parsun.parsun_api.receivables.application.dto.response.PaymentResultResponse;
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
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PayAllCustomerDebtUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusService invoiceStatusService;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;

    public PayAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                      PaymentAllocationRepository allocationRepository,
                                      InvoiceRepository invoiceRepository,
                                      InvoiceStatusService invoiceStatusService,
                                      CreditNoteRepository creditNoteRepository,
                                      DebitNoteRepository debitNoteRepository,
                                      ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceStatusService = invoiceStatusService;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.consumeCustomerBalanceUseCase = consumeCustomerBalanceUseCase;
    }

    @Transactional
    public PaymentResultResponse execute(UUID customerId, PayCustomerDebtRequest request,
                                          UUID authenticatedUserId,
                                          String authenticatedUserName) {
        List<String> statuses = List.of("PENDING_PAYMENT", "PARTIALLY_PAID");
        List<Invoice> unpaidInvoices = invoiceRepository.findAllWithFilters(
                null, null, customerId, statuses, null, null, null, null, null);

        if (unpaidInvoices.isEmpty()) {
            throw new IllegalArgumentException("Customer has no unpaid invoices: " + customerId);
        }

        BigDecimal totalAmount = unpaidInvoices.stream()
                .map(inv -> {
                    BigDecimal creds = creditNoteRepository.findByInvoiceId(inv.getId().value()).stream()
                            .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal debits = debitNoteRepository.findByInvoiceId(inv.getId().value()).stream()
                            .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return inv.calculateTotal(creds, debits);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balanceUsed = BigDecimal.ZERO;
        if (request.useBalance() != null && request.useBalance() && request.balanceAmount() != null) {
            balanceUsed = request.balanceAmount();
            if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
                consumeCustomerBalanceUseCase.execute(
                    customerId,
                    BalanceType.INVOICES,
                    balanceUsed,
                    BalanceType.INVOICES,
                    null,
                    "Pago total de deuda",
                    null,
                    authenticatedUserId
                );
            }
        }

        BigDecimal paymentAmount = totalAmount.subtract(balanceUsed);

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0
                && balanceUsed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0 && request.paymentMethodId() == null) {
            throw new IllegalArgumentException("Payment method is required when payment amount is greater than zero");
        }

        int year = LocalDate.now().getYear();

        if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
            int sequence = paymentRepository.getNextSequenceForYear(year);
            PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

            Payment payment = Payment.create(
                    paymentNumber,
                    customerId,
                    paymentAmount,
                    request.paymentMethodId(),
                    request.referenceNumber(),
                    request.paymentDate(),
                    "Pago total de deuda del cliente",
                    authenticatedUserId,
                    authenticatedUserName,
                    PaymentStatus.APPLIED
            );

            Payment saved = paymentRepository.save(payment);

            for (Invoice invoice : unpaidInvoices) {
                BigDecimal creds = creditNoteRepository.findByInvoiceId(invoice.getId().value()).stream()
                        .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal debits = debitNoteRepository.findByInvoiceId(invoice.getId().value()).stream()
                        .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                PaymentAllocation allocation = PaymentAllocation.create(
                        saved.getId().value(),
                        invoice.getId().value(),
                        invoice.calculateTotal(creds, debits)
                );
                allocationRepository.save(allocation);
            }

            payment.apply(paymentAmount);
            paymentRepository.save(payment);
        }

        if (balanceUsed.compareTo(BigDecimal.ZERO) > 0) {
            int balanceSequence = paymentRepository.getNextSequenceForYear(year);
            PaymentNumber balancePaymentNumber = PaymentNumber.of(year, balanceSequence);

            Payment balancePayment = Payment.create(
                    balancePaymentNumber,
                    customerId,
                    balanceUsed,
                    null,
                    PaymentSource.CUSTOMER_BALANCE,
                    null,
                    request.paymentDate(),
                    "Pago con saldo a favor - Pago total de deuda",
                    authenticatedUserId,
                    authenticatedUserName,
                    PaymentStatus.APPLIED
            );

            Payment savedBalancePayment = paymentRepository.save(balancePayment);

            BigDecimal remainingBalance = balanceUsed;
            for (Invoice invoice : unpaidInvoices) {
                if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal creds = creditNoteRepository.findByInvoiceId(invoice.getId().value()).stream()
                        .map(c -> c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal debits = debitNoteRepository.findByInvoiceId(invoice.getId().value()).stream()
                        .map(d -> d.getAmount() != null ? d.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal invoiceTotal = invoice.calculateTotal(creds, debits);

                BigDecimal alreadyAllocated = allocationRepository.findByInvoiceId(invoice.getId().value()).stream()
                        .map(PaymentAllocation::getAllocatedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal invoiceRemaining = invoiceTotal.subtract(alreadyAllocated);
                if (invoiceRemaining.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal allocAmount = remainingBalance.min(invoiceRemaining);
                PaymentAllocation balanceAllocation = PaymentAllocation.create(
                        savedBalancePayment.getId().value(),
                        invoice.getId().value(),
                        allocAmount
                );
                allocationRepository.save(balanceAllocation);
                remainingBalance = remainingBalance.subtract(allocAmount);
            }

            balancePayment.apply(balanceUsed);
            paymentRepository.save(balancePayment);
        }

        for (Invoice invoice : unpaidInvoices) {
            invoiceStatusService.recalculate(invoice.getId().value());
        }

        return new PaymentResultResponse(
                null,
                PaymentStatus.APPLIED.name(),
                "Pago total registrado exitosamente",
                true
        );
    }
}
