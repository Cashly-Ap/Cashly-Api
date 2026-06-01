package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.request.PayCustomerDebtRequest;
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

public class PayAllCustomerDebtUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;

    public PayAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                      PaymentAllocationRepository allocationRepository,
                                      InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
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
                .map(Invoice::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                customerId,
                PaymentType.PAYMENT,
                totalAmount,
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                "Pago total de deuda del cliente",
                authenticatedUserId,
                authenticatedUserName
        );

        Payment saved = paymentRepository.save(payment);

        for (Invoice invoice : unpaidInvoices) {
            PaymentAllocation allocation = PaymentAllocation.create(
                    saved.getId().value(),
                    invoice.getId().value(),
                    invoice.getSubtotal()
            );
            allocationRepository.save(allocation);
        }

        payment.apply(totalAmount);
        paymentRepository.save(payment);

        return new PaymentResultResponse(
                saved.getId().value(),
                payment.getStatus().name(),
                "Pago total registrado exitosamente",
                true
        );
    }
}
