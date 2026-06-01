package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.request.PayPartialDebtRequest;
import com.parsun.parsun_api.receivables.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PayPartialDebtUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;

    public PayPartialDebtUseCase(PaymentRepository paymentRepository,
                                  PaymentAllocationRepository allocationRepository,
                                  InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public PaymentResultResponse execute(UUID customerId, PayPartialDebtRequest request,
                                          UUID authenticatedUserId,
                                          String authenticatedUserName) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        List<String> statuses = List.of("PENDING_PAYMENT", "PARTIALLY_PAID");
        List<Invoice> unpaidInvoices = invoiceRepository.findAllWithFilters(
                null, null, customerId, statuses, null, null, null, null, null);

        if (unpaidInvoices.isEmpty()) {
            throw new IllegalArgumentException("Customer has no unpaid invoices: " + customerId);
        }

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                customerId,
                PaymentType.PAYMENT,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                "Pago parcial de deuda del cliente",
                authenticatedUserId,
                authenticatedUserName
        );

        Payment saved = paymentRepository.save(payment);

        BigDecimal remaining = request.amount();
        for (Invoice invoice : unpaidInvoices) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal allocAmount = remaining.min(invoice.getSubtotal());
            PaymentAllocation allocation = PaymentAllocation.create(
                    saved.getId().value(),
                    invoice.getId().value(),
                    allocAmount
            );
            allocationRepository.save(allocation);
            remaining = remaining.subtract(allocAmount);
        }

        payment.apply(request.amount());
        paymentRepository.save(payment);

        return new PaymentResultResponse(
                saved.getId().value(),
                payment.getStatus().name(),
                "Pago parcial registrado exitosamente",
                true
        );
    }
}
