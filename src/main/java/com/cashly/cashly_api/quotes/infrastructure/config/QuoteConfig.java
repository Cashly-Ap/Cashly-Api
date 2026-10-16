package com.cashly.cashly_api.quotes.infrastructure.config;

import com.cashly.cashly_api.quotes.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteCustomerGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.assign_warehouses.AssignQuoteWarehousesUseCase;
import com.cashly.cashly_api.quotes.application.cancel.CancelQuoteUseCase;
import com.cashly.cashly_api.quotes.application.convert_to_invoice.ConvertQuoteToInvoiceUseCase;
import com.cashly.cashly_api.quotes.application.convert_to_order.ConvertQuoteToOrderUseCase;
import com.cashly.cashly_api.quotes.application.create.CreateQuoteUseCase;
import com.cashly.cashly_api.quotes.application.delete.DeleteQuoteUseCase;
import com.cashly.cashly_api.quotes.application.products.GetProductsForQuoteUseCase;
import com.cashly.cashly_api.quotes.application.get.GetQuoteByIdUseCase;
import com.cashly.cashly_api.quotes.application.list.GetQuotesUseCase;
import com.cashly.cashly_api.quotes.application.warehouses.GetWarehousesForQuoteUseCase;
import com.cashly.cashly_api.quotes.application.update.UpdateQuoteUseCase;
import com.cashly.cashly_api.quotes.domain.QuoteRepository;
import com.cashly.cashly_api.quotes.infrastructure.persistence.adapters.JpaQuoteRepositoryAdapter;
import com.cashly.cashly_api.quotes.infrastructure.persistence.mappers.QuoteJpaMapper;
import com.cashly.cashly_api.quotes.infrastructure.persistence.repositories.QuoteJpaRepository;
import com.cashly.cashly_api.quotes.infrastructure.persistence.repositories.QuoteNumberSequenceJpaRepository;
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
                                              com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway products) {
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
                                                  com.cashly.cashly_api.quotes.application.ports.out.QuoteRejectionNotificationGateway notificationService) {
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
            com.cashly.cashly_api.quotes.application.ports.out.OrderCreationGateway orders,
            com.cashly.cashly_api.quotes.application.ports.out.ActorIdentityGateway actors,
            com.cashly.cashly_api.quotes.application.ports.out.QuoteConversionNotificationGateway notifications) {
        return new ConvertQuoteToOrderUseCase(quoteRepository, orders, actors, notifications);
    }

    @Bean
    public ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase(
            QuoteRepository quoteRepository,
            com.cashly.cashly_api.quotes.application.ports.out.InvoiceCreationGateway invoices,
            com.cashly.cashly_api.quotes.application.ports.out.QuoteConversionNotificationGateway notifications) {
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
