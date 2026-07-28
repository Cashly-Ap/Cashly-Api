package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.portal.application.dto.response.PortalInvoiceDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalInvoiceListResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalInvoiceMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class GetCustomerInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    public GetCustomerInvoicesUseCase(InvoiceRepository invoiceRepository,
                                       PaymentAllocationRepository paymentAllocationRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
    }

    public List<PortalInvoiceListResponse> execute(String customerId) {
        UUID customerUuid = UUID.fromString(customerId);
        return invoiceRepository.findAllWithFilters(null, null, customerUuid, null, null, null, null, "createdAt", "desc")
            .stream()
            .map(invoice -> {
                BigDecimal paidAmount = paymentAllocationRepository.getAllocatedAmountByInvoiceId(invoice.getId().value());
                BigDecimal balance = invoice.getSubtotal().subtract(paidAmount);
                if (balance.compareTo(BigDecimal.ZERO) < 0) {
                    balance = BigDecimal.ZERO;
                }
                return PortalInvoiceMapper.toListResponse(invoice, balance, paidAmount);
            })
            .toList();
    }

    public List<PortalInvoiceListResponse> execute(String customerId, String search, String status) {
        UUID customerUuid = UUID.fromString(customerId);
        List<String> statuses = status != null && !status.isBlank() ? List.of(status) : null;
        return invoiceRepository.findAllWithFilters(null, null, customerUuid, statuses, null, null, null, "createdAt", "desc")
            .stream()
            .filter(invoice -> search == null || search.isBlank()
                || invoice.getInvoiceNumber().value().toLowerCase().contains(search.toLowerCase()))
            .map(invoice -> {
                BigDecimal paidAmount = paymentAllocationRepository.getAllocatedAmountByInvoiceId(invoice.getId().value());
                BigDecimal balance = invoice.getSubtotal().subtract(paidAmount);
                if (balance.compareTo(BigDecimal.ZERO) < 0) {
                    balance = BigDecimal.ZERO;
                }
                return PortalInvoiceMapper.toListResponse(invoice, balance, paidAmount);
            })
            .toList();
    }

    public PortalInvoiceDetailResponse execute(String customerId, String invoiceId) {
        UUID customerUuid = UUID.fromString(customerId);
        InvoiceId invoiceIdObj = InvoiceId.of(UUID.fromString(invoiceId));

        var invoice = invoiceRepository.findById(invoiceIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        if (!invoice.getCustomerId().equals(customerUuid)) {
            throw new IllegalArgumentException("Invoice not found");
        }

        BigDecimal paidAmount = paymentAllocationRepository.getAllocatedAmountByInvoiceId(invoice.getId().value());
        BigDecimal balance = invoice.getSubtotal().subtract(paidAmount);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balance = BigDecimal.ZERO;
        }

        return PortalInvoiceMapper.toDetailResponse(invoice, balance, paidAmount);
    }
}
