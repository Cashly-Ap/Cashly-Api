package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.request.PayInvoiceRequest;
import com.parsun.parsun_api.receivables.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.domain.entities.PaymentAllocation;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentNumber;
import com.parsun.parsun_api.payments.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PayInvoiceUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;

    public PayInvoiceUseCase(PaymentRepository paymentRepository,
                              PaymentAllocationRepository allocationRepository,
                              InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public PaymentResultResponse execute(UUID invoiceId, PayInvoiceRequest request,
                                          UUID authenticatedUserId,
                                          String authenticatedUserName) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal amount = request.amount() != null ? request.amount() : invoice.calculateTotal();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        int year = LocalDate.now().getYear();
        int sequence = paymentRepository.getNextSequenceForYear(year);
        PaymentNumber paymentNumber = PaymentNumber.of(year, sequence);

        Payment payment = Payment.create(
                paymentNumber,
                invoice.getCustomerId(),
                PaymentType.PAYMENT,
                amount,
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                "Pago de factura " + invoice.getInvoiceNumber().value(),
                authenticatedUserId,
                authenticatedUserName
        );

        Payment saved = paymentRepository.save(payment);

        PaymentAllocation allocation = PaymentAllocation.create(
                saved.getId().value(),
                invoiceId,
                amount
        );
        allocationRepository.save(allocation);

        payment.apply(amount);
        paymentRepository.save(payment);

        return new PaymentResultResponse(
                saved.getId().value(),
                payment.getStatus().name(),
                "Pago registrado exitosamente",
                true
        );
    }
}
