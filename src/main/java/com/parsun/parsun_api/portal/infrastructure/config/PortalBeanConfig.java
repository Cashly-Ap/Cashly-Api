package com.parsun.parsun_api.portal.infrastructure.config;

import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.auth.authentication.infrastructure.adapters.CustomerRifResolver;
import com.parsun.parsun_api.auth.token.application.usecases.LogoutUserUseCase;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerBalanceRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import com.parsun.parsun_api.invoices.application.usecases.GetInvoicesUseCase;
import com.parsun.parsun_api.orders.application.usecases.CancelOrderUseCase;
import com.parsun.parsun_api.orders.application.usecases.ConfirmOrderUseCase;
import com.parsun.parsun_api.orders.application.usecases.CreateOrderUseCase;
import com.parsun.parsun_api.orders.application.usecases.GetOrderByIdUseCase;
import com.parsun.parsun_api.orders.application.usecases.GetOrdersUseCase;
import com.parsun.parsun_api.orders.application.usecases.UpdateOrderUseCase;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.payments.application.usecases.GetAllPaymentsUseCase;
import com.parsun.parsun_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.parsun.parsun_api.payments.application.usecases.CreatePaymentUseCase;
import com.parsun.parsun_api.payments.application.usecases.PayAllCustomerDebtUseCase;
import com.parsun.parsun_api.payments.application.usecases.PayInvoiceUseCase;
import com.parsun.parsun_api.payments.application.usecases.PayPartialDebtUseCase;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.portal.application.usecases.CancelPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.CancelPortalQuoteUseCase;
import com.parsun.parsun_api.portal.application.usecases.ConfirmPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.ConvertPortalQuoteToOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.ChangeCustomerPasswordUseCase;
import com.parsun.parsun_api.portal.application.usecases.CreatePortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.CreatePortalQuoteUseCase;
import com.parsun.parsun_api.portal.application.usecases.GeneratePortalCatalogPdfUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetCustomerProfileUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalDashboardUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalInvoiceUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalPaymentUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalQuoteUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalInvoicesUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalOrdersUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalPaymentsUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalQuotesUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListProductsUseCase;
import com.parsun.parsun_api.portal.application.usecases.UpdatePortalOrderUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalAllDebtUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalInvoiceUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalPartialDebtUseCase;
import com.parsun.parsun_api.portal.application.usecases.PortalLoginUseCase;
import com.parsun.parsun_api.portal.application.usecases.RegisterPortalPaymentUseCase;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import com.parsun.parsun_api.quotes.application.usecases.CancelQuoteUseCase;
import com.parsun.parsun_api.quotes.application.usecases.ConvertQuoteToOrderUseCase;
import com.parsun.parsun_api.quotes.application.usecases.CreateQuoteUseCase;
import com.parsun.parsun_api.quotes.application.usecases.GetQuoteByIdUseCase;
import com.parsun.parsun_api.quotes.application.usecases.GetQuotesUseCase;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PortalBeanConfig {

    @Bean
    public PortalLoginUseCase portalLoginUseCase(
            CustomerRifResolver customerRifResolver,
            RefreshTokenRepository refreshTokenRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder) {
        return new PortalLoginUseCase(
                customerRifResolver,
                refreshTokenRepository,
                tokenService,
                passwordEncoder);
    }

    @Bean
    public ChangeCustomerPasswordUseCase changeCustomerPasswordUseCase(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        return new ChangeCustomerPasswordUseCase(customerRepository, passwordEncoder);
    }

    @Bean
    public ListProductsUseCase portalListProductsUseCase(
            CustomerRepository customerRepository,
            CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            WarehouseStockRepository warehouseStockRepository) {
        return new ListProductsUseCase(
                customerRepository,
                customerWarehouseAccessRepository,
                productRepository,
                categoryRepository,
                warehouseStockRepository);
    }

    @Bean
    public GeneratePortalCatalogPdfUseCase generatePortalCatalogPdfUseCase(
            ListProductsUseCase portalListProductsUseCase,
            CommercialCatalogPdfPort commercialCatalogPdfPort,
            FileStorageService fileStorageService) {
        return new GeneratePortalCatalogPdfUseCase(
                portalListProductsUseCase,
                commercialCatalogPdfPort,
                fileStorageService);
    }

    @Bean
    public GetCustomerProfileUseCase getCustomerProfileUseCase(
            CustomerRepository customerRepository,
            CustomerBalanceRepository customerBalanceRepository) {
        return new GetCustomerProfileUseCase(customerRepository, customerBalanceRepository);
    }

    @Bean
    public GetPortalDashboardUseCase getPortalDashboardUseCase(
            QuoteRepository quoteRepository,
            OrderRepository orderRepository,
            InvoiceRepository invoiceRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            CreditNoteRepository creditNoteRepository,
            DebitNoteRepository debitNoteRepository,
            PortalAuthorizationService portalAuthorizationService) {
        return new GetPortalDashboardUseCase(
                quoteRepository,
                orderRepository,
                invoiceRepository,
                paymentAllocationRepository,
                creditNoteRepository,
                debitNoteRepository,
                portalAuthorizationService);
    }

    @Bean
    public ListPortalQuotesUseCase listPortalQuotesUseCase(
            GetQuotesUseCase getQuotesUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new ListPortalQuotesUseCase(getQuotesUseCase, portalAuthorizationService);
    }

    @Bean
    public GetPortalQuoteUseCase getPortalQuoteUseCase(
            GetQuoteByIdUseCase getQuoteByIdUseCase) {
        return new GetPortalQuoteUseCase(getQuoteByIdUseCase);
    }

    @Bean
    public CreatePortalQuoteUseCase createPortalQuoteUseCase(
            CreateQuoteUseCase createQuoteUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new CreatePortalQuoteUseCase(createQuoteUseCase, portalAuthorizationService);
    }

    @Bean
    public CancelPortalQuoteUseCase cancelPortalQuoteUseCase(
            CancelQuoteUseCase cancelQuoteUseCase) {
        return new CancelPortalQuoteUseCase(cancelQuoteUseCase);
    }

    @Bean
    public ConvertPortalQuoteToOrderUseCase convertPortalQuoteToOrderUseCase(
            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase) {
        return new ConvertPortalQuoteToOrderUseCase(convertQuoteToOrderUseCase);
    }

    @Bean
    public ListPortalOrdersUseCase listPortalOrdersUseCase(
            GetOrdersUseCase getOrdersUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new ListPortalOrdersUseCase(getOrdersUseCase, portalAuthorizationService);
    }

    @Bean
    public GetPortalOrderUseCase getPortalOrderUseCase(
            GetOrderByIdUseCase getOrderByIdUseCase) {
        return new GetPortalOrderUseCase(getOrderByIdUseCase);
    }

    @Bean
    public ConfirmPortalOrderUseCase confirmPortalOrderUseCase(
            ConfirmOrderUseCase confirmOrderUseCase) {
        return new ConfirmPortalOrderUseCase(confirmOrderUseCase);
    }

    @Bean
    public CancelPortalOrderUseCase cancelPortalOrderUseCase(
            CancelOrderUseCase cancelOrderUseCase) {
        return new CancelPortalOrderUseCase(cancelOrderUseCase);
    }

    @Bean
    public CreatePortalOrderUseCase createPortalOrderUseCase(
            CreateOrderUseCase createOrderUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new CreatePortalOrderUseCase(createOrderUseCase, portalAuthorizationService);
    }

    @Bean
    public UpdatePortalOrderUseCase updatePortalOrderUseCase(
            UpdateOrderUseCase updateOrderUseCase) {
        return new UpdatePortalOrderUseCase(updateOrderUseCase);
    }

    @Bean
    public ListPortalInvoicesUseCase listPortalInvoicesUseCase(
            GetInvoicesUseCase getInvoicesUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new ListPortalInvoicesUseCase(getInvoicesUseCase, portalAuthorizationService);
    }

    @Bean
    public GetPortalInvoiceUseCase getPortalInvoiceUseCase(
            GetInvoiceByIdUseCase getInvoiceByIdUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new GetPortalInvoiceUseCase(getInvoiceByIdUseCase, portalAuthorizationService);
    }

    @Bean
    public ListPortalPaymentsUseCase listPortalPaymentsUseCase(
            GetAllPaymentsUseCase getAllPaymentsUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new ListPortalPaymentsUseCase(getAllPaymentsUseCase, portalAuthorizationService);
    }

    @Bean
    public GetPortalPaymentUseCase getPortalPaymentUseCase(
            GetPaymentByIdUseCase getPaymentByIdUseCase) {
        return new GetPortalPaymentUseCase(getPaymentByIdUseCase);
    }

    @Bean
    public PayPortalInvoiceUseCase payPortalInvoiceUseCase(
            PayInvoiceUseCase payInvoiceUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new PayPortalInvoiceUseCase(payInvoiceUseCase, portalAuthorizationService);
    }

    @Bean
    public PayPortalAllDebtUseCase payPortalAllDebtUseCase(
            PayAllCustomerDebtUseCase payAllCustomerDebtUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new PayPortalAllDebtUseCase(payAllCustomerDebtUseCase, portalAuthorizationService);
    }

    @Bean
    public PayPortalPartialDebtUseCase payPortalPartialDebtUseCase(
            PayPartialDebtUseCase payPartialDebtUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new PayPortalPartialDebtUseCase(payPartialDebtUseCase, portalAuthorizationService);
    }

    @Bean
    public RegisterPortalPaymentUseCase registerPortalPaymentUseCase(
            CreatePaymentUseCase createPaymentUseCase,
            PortalAuthorizationService portalAuthorizationService,
            FileStorageService fileStorageService,
            NotificationService notificationService,
            CustomerRepository customerRepository) {
        return new RegisterPortalPaymentUseCase(
                createPaymentUseCase, portalAuthorizationService, fileStorageService,
                notificationService, customerRepository);
    }
}
