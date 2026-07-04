package com.parsun.parsun_api.quotes.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.invoices.application.services.InvoiceAuthorizationService;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.quotes.application.usecases.*;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.infrastructure.persistence.JpaQuoteRepositoryAdapter;
import com.parsun.parsun_api.quotes.infrastructure.persistence.QuoteJpaMapper;
import com.parsun.parsun_api.quotes.infrastructure.persistence.QuoteJpaRepository;
import com.parsun.parsun_api.quotes.infrastructure.persistence.QuoteNumberSequenceJpaRepository;
import com.parsun.parsun_api.quotes.infrastructure.web.QuoteController;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
    public CancelQuoteUseCase cancelQuoteUseCase(QuoteRepository quoteRepository) {
        return new CancelQuoteUseCase(quoteRepository);
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
    public ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase(
            QuoteRepository quoteRepository,
            OrderRepository orderRepository,
            WarehouseStockRepository warehouseStockRepository,
            UserRepository userRepository) {
        return new ConvertQuoteToOrderUseCase(quoteRepository, orderRepository,
                warehouseStockRepository, userRepository);
    }

    @Bean
    public ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase(
            QuoteRepository quoteRepository,
            InvoiceRepository invoiceRepository,
            WarehouseStockRepository warehouseStockRepository,
            UserRepository userRepository,
            InvoiceAuthorizationService authorizationService) {
        return new ConvertQuoteToInvoiceUseCase(quoteRepository, invoiceRepository,
                warehouseStockRepository, userRepository, authorizationService);
    }

    @Bean
    public QuoteController quoteController(CreateQuoteUseCase createQuoteUseCase,
                                            GetQuotesUseCase getQuotesUseCase,
                                            GetQuoteByIdUseCase getQuoteByIdUseCase,
                                            UpdateQuoteUseCase updateQuoteUseCase,
                                            DeleteQuoteUseCase deleteQuoteUseCase,
                                            CancelQuoteUseCase cancelQuoteUseCase,
                                            GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase,
                                            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
                                            ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase) {
        return new QuoteController(createQuoteUseCase, getQuotesUseCase, getQuoteByIdUseCase,
                updateQuoteUseCase, deleteQuoteUseCase,
                cancelQuoteUseCase,
                getWarehousesForQuoteUseCase,
                convertQuoteToOrderUseCase, convertQuoteToInvoiceUseCase);
    }
}
