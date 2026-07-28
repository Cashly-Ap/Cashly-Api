package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.payments.domain.entities.Payment;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.portal.application.dto.response.PortalDashboardResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalDashboardMapper;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public class GetCustomerDashboardUseCase {

    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerBalanceRepository customerBalanceRepository;

    public GetCustomerDashboardUseCase(QuoteRepository quoteRepository,
                                        OrderRepository orderRepository,
                                        InvoiceRepository invoiceRepository,
                                        PaymentRepository paymentRepository,
                                        ProductRepository productRepository,
                                        CustomerRepository customerRepository,
                                        CustomerBalanceRepository customerBalanceRepository) {
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.customerBalanceRepository = customerBalanceRepository;
    }

    public PortalDashboardResponse execute(String customerId) {
        UUID customerUuid = UUID.fromString(customerId);
        CustomerId customerIdObj = new CustomerId(customerUuid);

        customerRepository.findById(customerIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        List<Quote> allQuotes = quoteRepository.findAllWithFilters(null, null, customerUuid, null, null, null, "createdAt", "desc");
        int draftQuotes = (int) allQuotes.stream()
            .filter(q -> q.getStatus().name().equals("DRAFT"))
            .count();

        List<Order> allOrders = orderRepository.findAllWithFilters(null, null, customerUuid, null, null, null, "createdAt", "desc");
        int confirmedOrders = (int) allOrders.stream()
            .filter(o -> o.getStatus().name().equals("CONFIRMED"))
            .count();

        List<Invoice> allInvoices = invoiceRepository.findAllWithFilters(null, null, customerUuid, null, null, null, null, "createdAt", "desc");
        int pendingInvoices = (int) allInvoices.stream()
            .filter(i -> {
                String status = i.getStatus().name();
                return status.equals("PENDING_PAYMENT") || status.equals("PARTIALLY_PAID");
            })
            .count();

        BigDecimal pendingAmount = customerBalanceRepository.findByCustomerId(customerUuid)
            .map(b -> b.getTotalBalance())
            .orElse(BigDecimal.ZERO);

        var productsPage = productRepository.findAllWithFilters(null, null, true, null, 0, 1, "name", "asc");
        int totalProducts = (int) productsPage.totalElements();

        List<Quote> recentQuotes = allQuotes.stream()
            .sorted(Comparator.comparing(Quote::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .toList();

        List<Order> recentOrders = allOrders.stream()
            .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .toList();

        List<Invoice> recentInvoices = allInvoices.stream()
            .sorted(Comparator.comparing(Invoice::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .toList();

        List<Payment> recentPayments = paymentRepository.findAllWithFilters(null, null, customerUuid, null)
            .stream()
            .sorted(Comparator.comparing(Payment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .toList();

        return PortalDashboardMapper.buildDashboard(
            draftQuotes,
            confirmedOrders,
            pendingInvoices,
            pendingAmount,
            totalProducts,
            recentQuotes,
            recentOrders,
            recentInvoices,
            recentPayments
        );
    }
}
