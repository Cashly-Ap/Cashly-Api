package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.portal.application.dto.response.PortalInvoiceDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalInvoiceItemResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalInvoiceListResponse;

import java.math.BigDecimal;
import java.util.List;

public final class PortalInvoiceMapper {

    private PortalInvoiceMapper() {}

    public static PortalInvoiceListResponse toListResponse(Invoice invoice, BigDecimal balance, BigDecimal paidAmount) {
        return new PortalInvoiceListResponse(
            invoice.getId().value(),
            invoice.getInvoiceNumber().value(),
            invoice.getInvoiceNumber().value(),
            invoice.getCreatedAt(),
            invoice.getCreatedAt(),
            invoice.getStatus().name(),
            invoice.getSubtotal(),
            invoice.getSubtotal(),
            balance,
            paidAmount,
            invoice.getPaymentType().name(),
            invoice.getCreatedAt(),
            BigDecimal.ZERO
        );
    }

    public static PortalInvoiceDetailResponse toDetailResponse(Invoice invoice, BigDecimal balance, BigDecimal paidAmount) {
        List<PortalInvoiceItemResponse> items = invoice.getLines().stream()
            .map(PortalInvoiceMapper::toItemResponse)
            .toList();

        BigDecimal tax = invoice.getSubtotal().multiply(new BigDecimal("0.16"));

        return new PortalInvoiceDetailResponse(
            invoice.getId().value(),
            invoice.getInvoiceNumber().value(),
            invoice.getInvoiceNumber().value(),
            invoice.getCreatedAt(),
            invoice.getCreatedAt(),
            invoice.getStatus().name(),
            invoice.getSubtotal(),
            invoice.getSubtotal(),
            tax,
            balance,
            paidAmount,
            invoice.getPaymentType().name(),
            invoice.getNotes(),
            invoice.getCreatedAt(),
            items,
            items
        );
    }

    private static PortalInvoiceItemResponse toItemResponse(InvoiceLine line) {
        return new PortalInvoiceItemResponse(
            line.getProductId(),
            line.getProductCode(),
            line.getProductName(),
            line.getQuantity(),
            line.getUnitPrice(),
            line.getLineTotal()
        );
    }
}
