package com.parsun.parsun_api.portal.application.mapper;

import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.portal.application.dto.response.PortalDashboardResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalRecentActivityResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PortalDashboardMapper {

    private PortalDashboardMapper() {}

    public static PortalDashboardResponse buildDashboard(
            int draftQuotes,
            int confirmedOrders,
            int pendingInvoices,
            BigDecimal pendingAmount,
            int totalProducts,
            List<Quote> recentQuotes,
            List<Order> recentOrders,
            List<Invoice> recentInvoices,
            List<Payment> recentPayments
    ) {
        List<PortalRecentActivityResponse> activity = new ArrayList<>();

        recentQuotes.forEach(q -> activity.add(new PortalRecentActivityResponse(
            "QUOTE",
            q.getQuoteNumber().value(),
            q.getExpiryDate(),
            q.getStatus().name(),
            q.getSubtotal(),
            q.getNotes() != null ? q.getNotes() : q.getQuoteNumber().value()
        )));

        recentOrders.forEach(o -> activity.add(new PortalRecentActivityResponse(
            "ORDER",
            o.getOrderNumber().value(),
            o.getCreatedAt().toLocalDate(),
            o.getStatus().name(),
            o.getSubtotal(),
            o.getNotes() != null ? o.getNotes() : o.getOrderNumber().value()
        )));

        recentInvoices.forEach(i -> activity.add(new PortalRecentActivityResponse(
            "INVOICE",
            i.getInvoiceNumber().value(),
            i.getCreatedAt().toLocalDate(),
            i.getStatus().name(),
            i.getSubtotal(),
            i.getNotes() != null ? i.getNotes() : i.getInvoiceNumber().value()
        )));

        recentPayments.forEach(p -> activity.add(new PortalRecentActivityResponse(
            "PAYMENT",
            p.getPaymentNumber().value(),
            p.getPaymentDate(),
            p.getStatus().name(),
            p.getAmount(),
            p.getReferenceNumber()
        )));

        activity.sort(Comparator.comparing(PortalRecentActivityResponse::date, Comparator.nullsLast(Comparator.reverseOrder())));

        List<PortalRecentActivityResponse> recent = activity.size() > 10
            ? activity.subList(0, 10)
            : activity;

        return new PortalDashboardResponse(
            draftQuotes,
            confirmedOrders,
            pendingInvoices,
            pendingAmount,
            totalProducts,
            recent
        );
    }
}
