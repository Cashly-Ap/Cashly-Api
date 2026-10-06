package com.cashly.cashly_api.quotes.infrastructure.config;

import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.invoices.application.services.InvoiceAuthorizationService;
import com.cashly.cashly_api.invoices.domain.repositories.InvoiceRepository;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.orders.domain.repositories.OrderRepository;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.quotes.application.usecases.*;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.infrastructure.persistence.JpaQuoteRepositoryAdapter;
import com.cashly.cashly_api.quotes.infrastructure.persistence.QuoteJpaMapper;
import com.cashly.cashly_api.quotes.infrastructure.persistence.QuoteJpaRepository;
import com.cashly.cashly_api.quotes.infrastructure.persistence.QuoteNumberSequenceJpaRepository;
import com.cashly.cashly_api.quotes.infrastructure.web.QuoteController;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuoteConfig {

    @Bean
    public QuoteJpaMapper quoteJpaMapper() {
        return new QuoteJpaMapper();
    }

    @Bean
    public QuoteRepository quoteRepository(QuoteJpaRepository jpaRepository,
                                            QuoteNumberSequenceJpaRepository sequenceJpaRepository,
                                            QuoteJpaMapper mapper) {
        return new JpaQuoteRepositoryAdapter(jpaRepository, sequenceJpaRepository, mapper);
    }

    @Bean
    public CreateQuoteUseCase createQuoteUseCase(QuoteRepository quoteRepository,
                                                   CustomerRepository customerRepository,
                                                   UserRepository userRepository,
                                                   ProductRepository productRepository,
                                                   WarehouseRepository warehouseRepository) {
        return new CreateQuoteUseCase(quoteRepository, customerRepository, userRepository,
                productRepository, warehouseRepository);
    }

    @Bean
    public GetQuotesUseCase getQuotesUseCase(QuoteRepository quoteRepository) {
        return new GetQuotesUseCase(quoteRepository);
    }

    @Bean
    public GetQuoteByIdUseCase getQuoteByIdUseCase(QuoteRepository quoteRepository,
                                                    WarehouseStockRepository warehouseStockRepository,
                                                    WarehouseRepository warehouseRepository) {
        return new GetQuoteByIdUseCase(quoteRepository, warehouseStockRepository, warehouseRepository);
    }

    @Bean
    public UpdateQuoteUseCase updateQuoteUseCase(QuoteRepository quoteRepository,
                                                   ProductRepository productRepository,
                                                   WarehouseRepository warehouseRepository) {
        return new UpdateQuoteUseCase(quoteRepository, productRepository, warehouseRepository);
    }

    @Bean
    public DeleteQuoteUseCase deleteQuoteUseCase(QuoteRepository quoteRepository) {
        return new DeleteQuoteUseCase(quoteRepository);
    }

    @Bean
    public CancelQuoteUseCase cancelQuoteUseCase(QuoteRepository quoteRepository,
                                                  NotificationService notificationService) {
        return new CancelQuoteUseCase(quoteRepository, notificationService);
    }

    @Bean
    public GetProductsForQuoteUseCase getProductsForQuoteUseCase(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        return new GetProductsForQuoteUseCase(customerRepository, productRepository, categoryRepository);
    }

    @Bean
    public GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase(
            CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
            WarehouseRepository warehouseRepository) {
        return new GetWarehousesForQuoteUseCase(customerWarehouseAccessRepository, warehouseRepository);
    }

    @Bean
    public AssignQuoteWarehousesUseCase assignQuoteWarehousesUseCase(
            QuoteRepository quoteRepository,
            CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
            WarehouseRepository warehouseRepository) {
        return new AssignQuoteWarehousesUseCase(quoteRepository, customerWarehouseAccessRepository, warehouseRepository);
    }

    @Bean
    public ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase(
            QuoteRepository quoteRepository,
            OrderRepository orderRepository,
            WarehouseStockRepository warehouseStockRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        return new ConvertQuoteToOrderUseCase(quoteRepository, orderRepository,
                warehouseStockRepository, userRepository, notificationService);
    }

    @Bean
    public ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase(
            QuoteRepository quoteRepository,
            InvoiceRepository invoiceRepository,
            WarehouseStockRepository warehouseStockRepository,
            UserRepository userRepository,
            InvoiceAuthorizationService authorizationService,
            NotificationService notificationService,
            CustomerRepository customerRepository) {
        return new ConvertQuoteToInvoiceUseCase(quoteRepository, invoiceRepository,
                warehouseStockRepository, userRepository, authorizationService, notificationService,
                customerRepository);
    }

    @Bean
    public QuoteController quoteController(CreateQuoteUseCase createQuoteUseCase,
                                            GetQuotesUseCase getQuotesUseCase,
                                            GetQuoteByIdUseCase getQuoteByIdUseCase,
                                            UpdateQuoteUseCase updateQuoteUseCase,
                                            DeleteQuoteUseCase deleteQuoteUseCase,
                                            CancelQuoteUseCase cancelQuoteUseCase,
                                            GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase,
                                            AssignQuoteWarehousesUseCase assignQuoteWarehousesUseCase,
                                            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
                                            ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase) {
        return new QuoteController(createQuoteUseCase, getQuotesUseCase, getQuoteByIdUseCase,
                updateQuoteUseCase, deleteQuoteUseCase,
                cancelQuoteUseCase,
                getWarehousesForQuoteUseCase,
                assignQuoteWarehousesUseCase,
                convertQuoteToOrderUseCase, convertQuoteToInvoiceUseCase);
    }
}
