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
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PayAllCustomerDebtUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final FileStorageService fileStorageService;

    public PayAllCustomerDebtUseCase(PaymentRepository paymentRepository,
                                      PaymentAllocationRepository allocationRepository,
                                      InvoiceRepository invoiceRepository,
                                      FileStorageService fileStorageService) {
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
        this.invoiceRepository = invoiceRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public PaymentResultResponse execute(UUID customerId, PayCustomerDebtRequest request,
                                          MultipartFile voucherFile, UUID authenticatedUserId,
                                          String authenticatedUserName) {
        List<String> statuses = List.of("PENDING_PAYMENT", "PARTIALLY_PAID");
        List<Invoice> unpaidInvoices = invoiceRepository.findAllWithFilters(
                null, null, customerId, statuses, null, null, null, null);

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

        for (Invoice invoice : unpaidInvoices) {
            PaymentAllocation allocation = PaymentAllocation.create(
                    saved.getId().value(),
                    invoice.getId().value(),
                    invoice.getSubtotal()
            );
            allocationRepository.save(allocation);
        }

        return new PaymentResultResponse(
                saved.getId().value(),
                saved.getStatus().name(),
                "Bulk payment registered and pending approval",
                true
        );
    }
}
