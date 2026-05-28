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
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PayInvoiceUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileStorageService fileStorageService;

    public PayInvoiceUseCase(PaymentRepository paymentRepository,
                              PaymentAllocationRepository allocationRepository,
                              InvoiceRepository invoiceRepository,
                              FileStorageService fileStorageService) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public PaymentResultResponse execute(UUID invoiceId, PayInvoiceRequest request,
                                          MultipartFile voucherFile, UUID authenticatedUserId,
                                          String authenticatedUserName) {
        Invoice invoice = invoiceRepository.findById(new InvoiceId(invoiceId))
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        BigDecimal amount = request.amount() != null ? request.amount() : invoice.getSubtotal();

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

        if (voucherFile != null && !voucherFile.isEmpty()) {
            try {
                String filename = "vouchers/" + UUID.randomUUID() + "_" + voucherFile.getOriginalFilename();
                fileStorageService.store(
                        filename,
                        new ByteArrayInputStream(voucherFile.getBytes()),
                        voucherFile.getContentType()
                );
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload voucher file", e);
            }
        }

        payment.submitForApproval();
        Payment saved = paymentRepository.save(payment);

        PaymentAllocation allocation = PaymentAllocation.create(
                saved.getId().value(),
                invoiceId,
                amount
        );
        allocationRepository.save(allocation);

        return new PaymentResultResponse(
                saved.getId().value(),
                saved.getStatus().name(),
                "Payment registered and pending approval",
                true
        );
    }
}
