package com.cashly.cashly_api.portal.infrastructure.config;

import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.auth.authentication.infrastructure.adapters.CustomerRifResolver;
import com.cashly.cashly_api.auth.token.application.usecases.LogoutUserUseCase;
import com.cashly.cashly_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerBalanceRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.invoices.application.usecases.GetInvoicesUseCase;
import com.cashly.cashly_api.orders.application.usecases.CancelOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.ConfirmOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.CreateOrderUseCase;
import com.cashly.cashly_api.orders.application.usecases.GetOrderByIdUseCase;
import com.cashly.cashly_api.orders.application.usecases.GetOrdersUseCase;
import com.cashly.cashly_api.orders.application.usecases.UpdateOrderUseCase;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.payments.application.usecases.GetAllPaymentsUseCase;
import com.cashly.cashly_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.cashly.cashly_api.payments.application.usecases.CreatePaymentUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayAllCustomerDebtUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayInvoiceUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayPartialDebtUseCase;
import com.cashly.cashly_api.payments.domain.repositories.PaymentAllocationRepository;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.portal.application.usecases.CancelPortalOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.CancelPortalQuoteUseCase;
import com.cashly.cashly_api.portal.application.usecases.ConfirmPortalOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.ConvertPortalQuoteToOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.ChangeCustomerPasswordUseCase;
import com.cashly.cashly_api.portal.application.usecases.CreatePortalOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.CreatePortalQuoteUseCase;
import com.cashly.cashly_api.portal.application.usecases.GeneratePortalCatalogPdfUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPendingPortalPaymentsUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetCustomerProfileUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPortalDashboardUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPortalInvoiceUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPortalOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPortalPaymentUseCase;
import com.cashly.cashly_api.portal.application.usecases.GetPortalQuoteUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListPortalInvoicesUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListPortalOrdersUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListPortalPaymentsUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListPortalQuotesUseCase;
import com.cashly.cashly_api.portal.application.usecases.ListProductsUseCase;
import com.cashly.cashly_api.portal.application.usecases.UpdatePortalOrderUseCase;
import com.cashly.cashly_api.portal.application.usecases.PayPortalAllDebtUseCase;
import com.cashly.cashly_api.portal.application.usecases.PayPortalInvoiceUseCase;
import com.cashly.cashly_api.portal.application.usecases.PayPortalPartialDebtUseCase;
import com.cashly.cashly_api.portal.application.usecases.PortalLoginUseCase;
import com.cashly.cashly_api.portal.application.usecases.RegisterPortalPaymentUseCase;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import com.cashly.cashly_api.quotes.application.usecases.CancelQuoteUseCase;
import com.cashly.cashly_api.quotes.application.usecases.ConvertQuoteToOrderUseCase;
import com.cashly.cashly_api.quotes.application.usecases.CreateQuoteUseCase;
import com.cashly.cashly_api.quotes.application.usecases.GetQuoteByIdUseCase;
import com.cashly.cashly_api.quotes.application.usecases.GetQuotesUseCase;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
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
            GetQuoteByIdUseCase getQuoteByIdUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new GetPortalQuoteUseCase(getQuoteByIdUseCase, portalAuthorizationService);
    }

    @Bean
    public CreatePortalQuoteUseCase createPortalQuoteUseCase(
            CreateQuoteUseCase createQuoteUseCase,
            PortalAuthorizationService portalAuthorizationService,
            NotificationService notificationService) {
        return new CreatePortalQuoteUseCase(createQuoteUseCase, portalAuthorizationService, notificationService);
    }

    @Bean
    public CancelPortalQuoteUseCase cancelPortalQuoteUseCase(
            CancelQuoteUseCase cancelQuoteUseCase,
            GetPortalQuoteUseCase getPortalQuoteUseCase) {
        return new CancelPortalQuoteUseCase(cancelQuoteUseCase, getPortalQuoteUseCase);
    }

    @Bean
    public ConvertPortalQuoteToOrderUseCase convertPortalQuoteToOrderUseCase(
            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
            GetPortalQuoteUseCase getPortalQuoteUseCase) {
        return new ConvertPortalQuoteToOrderUseCase(convertQuoteToOrderUseCase, getPortalQuoteUseCase);
    }

    @Bean
    public ListPortalOrdersUseCase listPortalOrdersUseCase(
            GetOrdersUseCase getOrdersUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new ListPortalOrdersUseCase(getOrdersUseCase, portalAuthorizationService);
    }

    @Bean
    public GetPortalOrderUseCase getPortalOrderUseCase(
            GetOrderByIdUseCase getOrderByIdUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new GetPortalOrderUseCase(getOrderByIdUseCase, portalAuthorizationService);
    }

    @Bean
    public ConfirmPortalOrderUseCase confirmPortalOrderUseCase(
            ConfirmOrderUseCase confirmOrderUseCase,
            GetPortalOrderUseCase getPortalOrderUseCase) {
        return new ConfirmPortalOrderUseCase(confirmOrderUseCase, getPortalOrderUseCase);
    }

    @Bean
    public CancelPortalOrderUseCase cancelPortalOrderUseCase(
            CancelOrderUseCase cancelOrderUseCase,
            GetPortalOrderUseCase getPortalOrderUseCase) {
        return new CancelPortalOrderUseCase(cancelOrderUseCase, getPortalOrderUseCase);
    }

    @Bean
    public CreatePortalOrderUseCase createPortalOrderUseCase(
            CreateOrderUseCase createOrderUseCase,
            PortalAuthorizationService portalAuthorizationService,
            NotificationService notificationService) {
        return new CreatePortalOrderUseCase(createOrderUseCase, portalAuthorizationService, notificationService);
    }

    @Bean
    public UpdatePortalOrderUseCase updatePortalOrderUseCase(
            UpdateOrderUseCase updateOrderUseCase,
            GetPortalOrderUseCase getPortalOrderUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new UpdatePortalOrderUseCase(updateOrderUseCase, getPortalOrderUseCase,
                portalAuthorizationService);
    }

    @Bean
    public ListPortalInvoicesUseCase listPortalInvoicesUseCase(
            GetInvoicesUseCase getInvoicesUseCase,
            PortalAuthorizationService portalAuthorizationService,
            PaymentRepository paymentRepository) {
        return new ListPortalInvoicesUseCase(getInvoicesUseCase, portalAuthorizationService, paymentRepository);
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
            GetPaymentByIdUseCase getPaymentByIdUseCase,
            PortalAuthorizationService portalAuthorizationService) {
        return new GetPortalPaymentUseCase(getPaymentByIdUseCase, portalAuthorizationService);
    }

    @Bean
    public PayPortalInvoiceUseCase payPortalInvoiceUseCase(
            CreatePaymentUseCase createPaymentUseCase,
            GetPortalInvoiceUseCase getPortalInvoiceUseCase,
            PortalAuthorizationService portalAuthorizationService,
            NotificationService notificationService,
            FileStorageService fileStorageService) {
        return new PayPortalInvoiceUseCase(createPaymentUseCase, getPortalInvoiceUseCase, portalAuthorizationService,
                notificationService, fileStorageService);
    }

    @Bean
    public PayPortalAllDebtUseCase payPortalAllDebtUseCase(
            CreatePaymentUseCase createPaymentUseCase,
            PortalAuthorizationService portalAuthorizationService,
            NotificationService notificationService,
            CustomerRepository customerRepository,
            FileStorageService fileStorageService) {
        return new PayPortalAllDebtUseCase(createPaymentUseCase, portalAuthorizationService,
                notificationService, customerRepository, fileStorageService);
    }

    @Bean
    public PayPortalPartialDebtUseCase payPortalPartialDebtUseCase(
            CreatePaymentUseCase createPaymentUseCase,
            PortalAuthorizationService portalAuthorizationService,
            NotificationService notificationService,
            CustomerRepository customerRepository,
            FileStorageService fileStorageService) {
        return new PayPortalPartialDebtUseCase(createPaymentUseCase, portalAuthorizationService,
                notificationService, customerRepository, fileStorageService);
    }

    @Bean
    public GetPendingPortalPaymentsUseCase getPendingPortalPaymentsUseCase(
            PaymentRepository paymentRepository,
            PortalAuthorizationService portalAuthorizationService) {
        return new GetPendingPortalPaymentsUseCase(paymentRepository, portalAuthorizationService);
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
