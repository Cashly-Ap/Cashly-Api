package com.parsun.parsun_api.portal.infrastructure.config;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.portal.application.usecase.*;
import com.parsun.parsun_api.portal.infrastructure.web.*;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortalConfig {

    @Bean
    public GetCustomerDashboardUseCase portalDashboardUseCase(
            QuoteRepository quoteRepository,
            OrderRepository orderRepository,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            CustomerBalanceRepository customerBalanceRepository) {
        return new GetCustomerDashboardUseCase(quoteRepository, orderRepository, invoiceRepository,
                paymentRepository, productRepository, customerRepository, customerBalanceRepository);
    }

    @Bean
    public GetCustomerQuotesUseCase portalQuotesUseCase(QuoteRepository quoteRepository) {
        return new GetCustomerQuotesUseCase(quoteRepository);
    }

    @Bean
    public GetCustomerOrdersUseCase portalOrdersUseCase(OrderRepository orderRepository) {
        return new GetCustomerOrdersUseCase(orderRepository);
    }

    @Bean
    public GetCustomerInvoicesUseCase portalInvoicesUseCase(
            InvoiceRepository invoiceRepository,
            PaymentAllocationRepository paymentAllocationRepository) {
        return new GetCustomerInvoicesUseCase(invoiceRepository, paymentAllocationRepository);
    }

    @Bean
    public GetCustomerPaymentsUseCase portalPaymentsUseCase(
            PaymentRepository paymentRepository,
            PaymentMethodRepository paymentMethodRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            InvoiceRepository invoiceRepository) {
        return new GetCustomerPaymentsUseCase(paymentRepository, paymentMethodRepository,
                paymentAllocationRepository, invoiceRepository);
    }

    @Bean
    public GetProductsUseCase portalProductsUseCase(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            WarehouseStockRepository warehouseStockRepository) {
        return new GetProductsUseCase(productRepository, categoryRepository, warehouseStockRepository);
    }

    @Bean
    public GetCustomerProfileUseCase portalProfileUseCase(
            CustomerRepository customerRepository,
            CustomerBalanceRepository customerBalanceRepository) {
        return new GetCustomerProfileUseCase(customerRepository, customerBalanceRepository);
    }

    @Bean
    public PortalDashboardController portalDashboardController(
            GetCustomerDashboardUseCase portalDashboardUseCase) {
        return new PortalDashboardController(portalDashboardUseCase);
    }

    @Bean
    public PortalQuoteController portalQuoteController(
            GetCustomerQuotesUseCase portalQuotesUseCase) {
        return new PortalQuoteController(portalQuotesUseCase);
    }

    @Bean
    public PortalOrderController portalOrderController(
            GetCustomerOrdersUseCase portalOrdersUseCase) {
        return new PortalOrderController(portalOrdersUseCase);
    }

    @Bean
    public PortalInvoiceController portalInvoiceController(
            GetCustomerInvoicesUseCase portalInvoicesUseCase) {
        return new PortalInvoiceController(portalInvoicesUseCase);
    }

    @Bean
    public PortalPaymentController portalPaymentController(
            GetCustomerPaymentsUseCase portalPaymentsUseCase) {
        return new PortalPaymentController(portalPaymentsUseCase);
    }

    @Bean
    public PortalProductController portalProductController(
            GetProductsUseCase portalProductsUseCase) {
        return new PortalProductController(portalProductsUseCase);
    }

    @Bean
    public PortalProfileController portalProfileController(
            GetCustomerProfileUseCase portalProfileUseCase) {
        return new PortalProfileController(portalProfileUseCase);
    }
}
