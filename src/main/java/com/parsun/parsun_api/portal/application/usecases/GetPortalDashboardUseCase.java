package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.portal.application.dto.response.PortalDashboardResponse;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetPortalDashboardUseCase {

    private static final List<String> PENDING_STATUSES = List.of("PENDING_PAYMENT", "PARTIALLY_PAID");

    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final PortalAuthorizationService portalAuthorizationService;

    public GetPortalDashboardUseCase(QuoteRepository quoteRepository,
                                      OrderRepository orderRepository,
                                      InvoiceRepository invoiceRepository,
                                      PaymentAllocationRepository paymentAllocationRepository,
                                      CreditNoteRepository creditNoteRepository,
                                      DebitNoteRepository debitNoteRepository,
                                      PortalAuthorizationService portalAuthorizationService) {
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public PortalDashboardResponse execute() {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

        List<Quote> quotes = quoteRepository.findAllWithFilters(
                null, null, customerId, null, null, null, null, null);
        List<Order> orders = orderRepository.findAllWithFilters(
                null, null, customerId, null, null, null, null, null);
        List<Invoice> pendingInvoices = invoiceRepository.findAllWithFilters(
                null, null, customerId, PENDING_STATUSES, null, null, null, null, null);

        long quoteCount = quotes.size();
        long orderCount = orders.size();
        long pendingInvoiceCount = pendingInvoices.size();
        BigDecimal totalPendingAmount = calculateTotalPendingAmount(pendingInvoices);
        List<PortalDashboardResponse.RecentActivityItem> recentActivity = buildRecentActivity(quotes, orders, pendingInvoices);

        return new PortalDashboardResponse(
                quoteCount,
                orderCount,
                pendingInvoiceCount,
                totalPendingAmount,
                recentActivity
        );
    }

    private BigDecimal calculateTotalPendingAmount(List<Invoice> pendingInvoices) {
        if (pendingInvoices.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<UUID> invoiceIds = pendingInvoices.stream()
                .map(invoice -> invoice.getId().value())
                .toList();

        Map<UUID, BigDecimal> paidAmounts = paymentAllocationRepository.sumByInvoiceIds(invoiceIds);
        Map<UUID, BigDecimal> creditApplied = creditNoteRepository.sumByInvoiceIds(invoiceIds);
        Map<UUID, BigDecimal> debitApplied = debitNoteRepository.sumByInvoiceIds(invoiceIds);

        return pendingInvoices.stream()
                .map(invoice -> {
                    UUID id = invoice.getId().value();
                    BigDecimal subtotal = invoice.getSubtotal();
                    BigDecimal paid = paidAmounts.getOrDefault(id, BigDecimal.ZERO);
                    BigDecimal credit = creditApplied.getOrDefault(id, BigDecimal.ZERO);
                    BigDecimal debit = debitApplied.getOrDefault(id, BigDecimal.ZERO);
                    return invoice.calculateTotal(credit, debit).subtract(paid);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<PortalDashboardResponse.RecentActivityItem> buildRecentActivity(
            List<Quote> quotes, List<Order> orders, List<Invoice> pendingInvoices) {

        List<PortalDashboardResponse.RecentActivityItem> items = new ArrayList<>();

        for (Quote quote : quotes) {
            items.add(new PortalDashboardResponse.RecentActivityItem(
                    "QUOTE",
                    quote.getId().toString(),
                    quote.getQuoteNumber().value(),
                    quote.getStatus().name(),
                    quote.getSubtotal(),
                    quote.getCreatedAt() != null
                            ? LocalDateTime.ofInstant(quote.getCreatedAt(), ZoneId.systemDefault()).toString()
                            : null
            ));
        }

        for (Order order : orders) {
            items.add(new PortalDashboardResponse.RecentActivityItem(
                    "ORDER",
                    order.getId().value().toString(),
                    order.getOrderNumber().value(),
                    order.getStatus().name(),
                    order.getSubtotal(),
                    order.getCreatedAt() != null ? order.getCreatedAt().toString() : null
            ));
        }

        for (Invoice invoice : pendingInvoices) {
            items.add(new PortalDashboardResponse.RecentActivityItem(
                    "INVOICE",
                    invoice.getId().value().toString(),
                    invoice.getInvoiceNumber().value(),
                    invoice.getStatus().name(),
                    invoice.getSubtotal(),
                    invoice.getCreatedAt() != null ? invoice.getCreatedAt().toString() : null
            ));
        }

        items.sort((a, b) -> {
            if (a.date() == null && b.date() == null) return 0;
            if (a.date() == null) return 1;
            if (b.date() == null) return -1;
            return b.date().compareTo(a.date());
        });

        int maxItems = Math.min(items.size(), 10);
        return items.subList(0, maxItems);
    }
}
