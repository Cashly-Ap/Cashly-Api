package com.cashly.cashly_api.sales.quotes.infrastructure.config;

import com.cashly.cashly_api.sales.quotes.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteCustomerGateway;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.sales.quotes.application.cancel.use_case.CancelQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.use_case.ConvertQuoteToInvoiceUseCase;
import com.cashly.cashly_api.sales.quotes.application.convert_to_order.use_case.ConvertQuoteToOrderUseCase;
import com.cashly.cashly_api.sales.quotes.application.create.use_case.CreateQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.delete.use_case.DeleteQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.products.use_case.GetProductsForQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.get.use_case.GetQuoteByIdUseCase;
import com.cashly.cashly_api.sales.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.sales.quotes.application.warehouses.use_case.GetWarehousesForQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.update.use_case.UpdateQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.adapters.JpaQuoteRepositoryAdapter;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.mappers.QuoteJpaMapper;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteJpaRepository;
import com.cashly.cashly_api.sales.quotes.infrastructure.persistence.repositories.QuoteNumberSequenceJpaRepository;
import com.cashly.cashly_api.sales.quotes.infrastructure.web.QuoteController;
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
        return new JpaQuoteRepositoryAdapter(jpaRepository,
                sequenceJpaRepository, mapper);
    }

    @Bean
    public CreateQuoteUseCase createQuoteUseCase(
            QuoteRepository quoteRepository, QuoteCustomerGateway customers,
            ActorIdentityGateway actors, QuoteProductCatalogGateway products,
            QuoteWarehouseGateway warehouses,
            ActivityNotificationRecorder activityNotifications) {
        return new CreateQuoteUseCase(quoteRepository, customers, actors,
                products, warehouses, activityNotifications);
    }

    @Bean
    public GetQuotesUseCase getQuotesUseCase(QuoteRepository quoteRepository,
            com.cashly.cashly_api.sales.quotes.application.ports.out.QuoteProductCatalogGateway products) {
        return new GetQuotesUseCase(quoteRepository, products);
    }

    @Bean
    public GetQuoteByIdUseCase getQuoteByIdUseCase(
            QuoteRepository quoteRepository, QuoteWarehouseGateway warehouses) {
        return new GetQuoteByIdUseCase(quoteRepository, warehouses);
    }

    @Bean
    public UpdateQuoteUseCase updateQuoteUseCase(
            QuoteRepository quoteRepository,
            QuoteProductCatalogGateway products,
            QuoteWarehouseGateway warehouses,
            ActivityNotificationRecorder activityNotifications) {
        return new UpdateQuoteUseCase(quoteRepository, products, warehouses,
                activityNotifications);
    }

    @Bean
    public DeleteQuoteUseCase deleteQuoteUseCase(
            QuoteRepository quoteRepository,
            ActivityNotificationRecorder activityNotifications) {
        return new DeleteQuoteUseCase(quoteRepository, activityNotifications);
    }

    @Bean
    public CancelQuoteUseCase cancelQuoteUseCase(
            QuoteRepository quoteRepository,
            ActivityNotificationRecorder activityNotifications) {
        return new CancelQuoteUseCase(quoteRepository, activityNotifications);
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
    public ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase(
            QuoteRepository quoteRepository,
            com.cashly.cashly_api.sales.quotes.application.ports.out.OrderCreationGateway orders,
            com.cashly.cashly_api.sales.quotes.application.ports.out.ActorIdentityGateway actors,
            ActivityNotificationRecorder activityNotifications) {
        return new ConvertQuoteToOrderUseCase(quoteRepository, orders, actors,
                activityNotifications);
    }

    @Bean
    public ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase(
            QuoteRepository quoteRepository,
            com.cashly.cashly_api.sales.quotes.application.ports.out.InvoiceCreationGateway invoices,
            ActivityNotificationRecorder activityNotifications) {
        return new ConvertQuoteToInvoiceUseCase(quoteRepository, invoices,
                activityNotifications);
    }

    @Bean
    public QuoteController quoteController(
            CreateQuoteUseCase createQuoteUseCase,
            GetQuotesUseCase getQuotesUseCase,
            GetQuoteByIdUseCase getQuoteByIdUseCase,
            UpdateQuoteUseCase updateQuoteUseCase,
            DeleteQuoteUseCase deleteQuoteUseCase,
            CancelQuoteUseCase cancelQuoteUseCase,
            GetWarehousesForQuoteUseCase getWarehousesForQuoteUseCase,
            ConvertQuoteToOrderUseCase convertQuoteToOrderUseCase,
            ConvertQuoteToInvoiceUseCase convertQuoteToInvoiceUseCase) {
        return new QuoteController(createQuoteUseCase, getQuotesUseCase,
                getQuoteByIdUseCase, updateQuoteUseCase, deleteQuoteUseCase,
                cancelQuoteUseCase, getWarehousesForQuoteUseCase,
                convertQuoteToOrderUseCase, convertQuoteToInvoiceUseCase);
    }
}
