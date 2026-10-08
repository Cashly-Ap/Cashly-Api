package com.cashly.cashly_api.quotes.infrastructure.config;

import com.cashly.cashly_api.quotes.application.contracts.ActorIdentityGateway;
import com.cashly.cashly_api.quotes.application.contracts.QuoteCustomerGateway;
import com.cashly.cashly_api.quotes.application.contracts.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.contracts.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.usecases.*;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.infrastructure.persistence.JpaQuoteRepositoryAdapter;
import com.cashly.cashly_api.quotes.infrastructure.persistence.QuoteJpaMapper;
import com.cashly.cashly_api.quotes.infrastructure.persistence.QuoteJpaRepository;
import com.cashly.cashly_api.quotes.infrastructure.persistence.QuoteNumberSequenceJpaRepository;
import com.cashly.cashly_api.quotes.infrastructure.web.QuoteController;
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
                                                   QuoteCustomerGateway customers,
                                                   ActorIdentityGateway actors,
                                                   QuoteProductCatalogGateway products,
                                                   QuoteWarehouseGateway warehouses) {
        return new CreateQuoteUseCase(quoteRepository, customers, actors, products, warehouses);
    }

    @Bean
    public GetQuotesUseCase getQuotesUseCase(QuoteRepository quoteRepository,
                                              com.cashly.cashly_api.quotes.application.contracts.QuoteProductCatalogGateway products) {
        return new GetQuotesUseCase(quoteRepository, products);
    }

    @Bean
    public GetQuoteByIdUseCase getQuoteByIdUseCase(QuoteRepository quoteRepository,
                                                    QuoteWarehouseGateway warehouses) {
        return new GetQuoteByIdUseCase(quoteRepository, warehouses);
    }

    @Bean
    public UpdateQuoteUseCase updateQuoteUseCase(QuoteRepository quoteRepository,
                                                   QuoteProductCatalogGateway products,
                                                   QuoteWarehouseGateway warehouses) {
        return new UpdateQuoteUseCase(quoteRepository, products, warehouses);
    }

    @Bean
    public DeleteQuoteUseCase deleteQuoteUseCase(QuoteRepository quoteRepository) {
        return new DeleteQuoteUseCase(quoteRepository);
    }

    @Bean
    public CancelQuoteUseCase cancelQuoteUseCase(QuoteRepository quoteRepository,
                                                  com.cashly.cashly_api.quotes.application.contracts.QuoteRejectionNotificationGateway notificationService) {
        return new CancelQuoteUseCase(quoteRepository, notificationService);
    }

    @Bean
    public GetProductsForQuoteUseCase getProductsForQuoteUseCase(
            QuoteCustomerGateway customers,
            QuoteProductCatalogGateway products) {
        return new GetProductsForQuoteUseCase(customers, products);
    }

    @Bean
    public GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase(
            QuoteWarehouseGateway warehouses) {
        return new GetWarehousesForQuoteUseCase(warehouses);
    }

    @Bean
    public AssignQuoteWarehousesUseCase assignQuoteWarehousesUseCase(
            QuoteRepository quoteRepository,
            QuoteWarehouseGateway warehouses) {
        return new AssignQuoteWarehousesUseCase(quoteRepository, warehouses);
    }

    @Bean
    public ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase(
            QuoteRepository quoteRepository,
            com.cashly.cashly_api.quotes.application.contracts.OrderCreationGateway orders,
            com.cashly.cashly_api.quotes.application.contracts.ActorIdentityGateway actors,
            com.cashly.cashly_api.quotes.application.contracts.QuoteConversionNotificationGateway notifications) {
        return new ConvertQuoteToOrderUseCase(quoteRepository, orders, actors, notifications);
    }

    @Bean
    public ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase(
            QuoteRepository quoteRepository,
            com.cashly.cashly_api.quotes.application.contracts.InvoiceCreationGateway invoices,
            com.cashly.cashly_api.quotes.application.contracts.QuoteConversionNotificationGateway notifications) {
        return new ConvertQuoteToInvoiceUseCase(quoteRepository, invoices, notifications);
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
