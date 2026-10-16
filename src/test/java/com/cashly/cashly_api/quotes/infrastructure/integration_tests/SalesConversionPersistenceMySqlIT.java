package com.cashly.cashly_api.quotes.infrastructure.integration_tests;

import com.cashly.cashly_api.quotes.infrastructure.persistence.adapters.JpaQuoteRepositoryAdapter;
import com.cashly.cashly_api.quotes.infrastructure.persistence.mappers.QuoteJpaMapper;
import com.cashly.cashly_api.quotes.infrastructure.persistence.repositories.*;
import com.cashly.cashly_api.quotes.application.convert_to_order.use_case.ConvertQuoteToOrderUseCase;
import com.cashly.cashly_api.quotes.application.convert_to_invoice.response.QuoteConvertedToInvoiceResponse;
import com.cashly.cashly_api.quotes.application.convert_to_invoice.use_case.ConvertQuoteToInvoiceUseCase;
import com.cashly.cashly_api.quotes.application.convert_to_invoice.request.ConvertQuoteToInvoiceRequest;
import com.cashly.cashly_api.quotes.application.ports.out.*;
import com.cashly.cashly_api.orders.infrastructure.persistence.adapters.OrderRepositoryAdapter;
import com.cashly.cashly_api.orders.infrastructure.persistence.repositories.*;
import com.cashly.cashly_api.orders.application.convert_to_invoice.use_case.ConvertToInvoiceUseCase;
import com.cashly.cashly_api.orders.application.convert_to_invoice.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.orders.application.ports.out.OrderNotificationGateway;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Regression for the real MySQL/Hibernate identity collision that returned HTTP 409 on conversion. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(initializers = SalesConversionPersistenceMySqlIT.IsolatedDatabaseOnly.class)
class SalesConversionPersistenceMySqlIT {
    public static class IsolatedDatabaseOnly implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            if (!"jdbc:mysql://suite-db:3306/screaming_suite".equals(context.getEnvironment().getProperty("spring.datasource.url"))) {
                throw new IllegalStateException("SalesConversionPersistenceMySqlIT requires the isolated verification database");
            }
        }
    }

    private static final java.util.concurrent.atomic.AtomicInteger QUOTE_SEQUENCE = new java.util.concurrent.atomic.AtomicInteger(9000);
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactions;
    @Autowired QuoteJpaRepository quoteJpa;
    @Autowired QuoteNumberSequenceJpaRepository quoteSequences;
    @Autowired OrderJpaRepository orderJpa;
    @Autowired OrderNumberSequenceJpaRepository orderSequences;

    private JpaQuoteRepositoryAdapter quotes() {
        return new JpaQuoteRepositoryAdapter(quoteJpa, quoteSequences, new QuoteJpaMapper());
    }

    @Test
    void convertsLoadedQuoteToOrderWithoutDuplicatingHistoricalCategorySnapshot() {
        UUID quoteId = seedQuote();
        UUID createdOrderId = UUID.randomUUID();
        var useCase = new ConvertQuoteToOrderUseCase(quotes(),
            command -> new OrderCreationGateway.CreatedOrder(createdOrderId, "PED-2026-9001"),
            mock(ActorIdentityGateway.class), mock(QuoteConversionNotificationGateway.class));

        useCase.execute(quoteId, null);
        entityManager.flush();
        entityManager.clear();

        assertThat(quotes().findById(quoteId).orElseThrow().getOrderId()).isEqualTo(createdOrderId);
        assertQuoteSnapshot(quoteId);
        assertThatThrownBy(() -> useCase.execute(quoteId, null))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("Only DRAFT");
    }

    @Test
    void convertsLoadedQuoteToInvoiceWithoutDuplicatingHistoricalCategorySnapshot() {
        UUID quoteId = seedQuote();
        UUID invoiceId = UUID.randomUUID();
        var useCase = new ConvertQuoteToInvoiceUseCase(quotes(),
            (command, actor) -> new InvoiceCreationGateway.CreatedInvoice(invoiceId, "FAC-2026-9001"),
            mock(QuoteConversionNotificationGateway.class));

        useCase.execute(quoteId, new ConvertQuoteToInvoiceRequest("CREDIT", null, false, null, null), null);
        entityManager.flush();
        entityManager.clear();

        assertThat(quotes().findById(quoteId).orElseThrow().getInvoiceId()).isEqualTo(invoiceId);
        assertQuoteSnapshot(quoteId);
    }

    @Test
    void invoicesLoadedOrderWithoutDuplicatingHistoricalCategorySnapshot() {
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        insertCustomer(customerId);
        insertCategory(categoryId, "Historical");
        insertProduct(productId, categoryId);
        insertOrder(orderId, customerId);
        insertLine(lineId, orderId, productId);
        jdbc.update("update orders set status = 'CONFIRMED' where id = UUID_TO_BIN(?)", orderId.toString());
        jdbc.update("insert into order_line_category_snapshots (order_line_id,category_id) values (UUID_TO_BIN(?),UUID_TO_BIN(?))", lineId.toString(), categoryId.toString());
        UUID invoiceId = UUID.randomUUID();
        var repository = new OrderRepositoryAdapter(orderJpa, orderSequences);
        var useCase = new ConvertToInvoiceUseCase(repository,
            (command, actor) -> new com.cashly.cashly_api.orders.application.ports.out.InvoiceCreationGateway.CreatedInvoice(invoiceId, "FAC-2026-9002"),
            mock(OrderNotificationGateway.class));

        useCase.execute(orderId, new ConvertToInvoiceRequest("CREDIT", null, false, null, null), null);
        entityManager.flush();
        entityManager.clear();

        var saved = repository.findById(com.cashly.cashly_api.orders.domain.valueobjects.OrderId.of(orderId)).orElseThrow();
        assertThat(saved.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(saved.getLines().getFirst().getCategoryId()).isEqualTo(categoryId);
        assertThat(jdbc.queryForObject("select count(*) from order_line_category_snapshots where order_line_id = UUID_TO_BIN(?)", Integer.class, lineId.toString())).isEqualTo(1);
    }

    @Test
    void rollsBackTargetAndSourceWhenConversionFailsAfterTargetCreation() {
        UUID targetId = UUID.randomUUID();
        var transaction = new TransactionTemplate(transactions);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        var fixture = transaction.execute(status -> seedQuoteFixture());
        try {
            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
                var useCase = new ConvertQuoteToOrderUseCase(quotes(), command -> {
                    insertOrder(targetId, command.customerId());
                    return new OrderCreationGateway.CreatedOrder(targetId, "PED-2026-9003");
                }, mock(ActorIdentityGateway.class), mock(QuoteConversionNotificationGateway.class));
                useCase.execute(fixture.quoteId(), null);
                entityManager.flush();
                throw new IllegalStateException("Simulated failure before transaction commit");
            })).isInstanceOf(IllegalStateException.class).hasMessage("Simulated failure before transaction commit");
            assertThat(jdbc.queryForObject("select count(*) from orders where id=UUID_TO_BIN(?)", Integer.class, targetId.toString())).isZero();
            assertThat(jdbc.queryForObject("select status from quotes where id=UUID_TO_BIN(?)", String.class, fixture.quoteId().toString())).isEqualTo("DRAFT");
            assertThat(jdbc.queryForObject("select count(*) from quotes where order_id=UUID_TO_BIN(?)", Integer.class, targetId.toString())).isZero();
            assertQuoteSnapshot(fixture.quoteId());
        } finally {
            transaction.executeWithoutResult(status -> {
                jdbc.update("delete from quote_line_category_snapshots where quote_line_id=UUID_TO_BIN(?)", fixture.lineId().toString());
                jdbc.update("delete from quote_lines where quote_id=UUID_TO_BIN(?)", fixture.quoteId().toString());
                jdbc.update("delete from quotes where id=UUID_TO_BIN(?)", fixture.quoteId().toString());
                jdbc.update("delete from products where id=UUID_TO_BIN(?)", fixture.productId().toString());
                jdbc.update("delete from categories where id=UUID_TO_BIN(?)", fixture.categoryId().toString());
                jdbc.update("delete from customers where id=UUID_TO_BIN(?)", fixture.customerId().toString());
            });
        }
    }

    @Test
    void persistsNewQuoteAndOrderWithHistoricalCategorySnapshots() {
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        insertCustomer(customerId);
        insertCategory(categoryId, "New snapshot");
        insertProduct(productId, categoryId);
        var quoteLine = new com.cashly.cashly_api.quotes.domain.entity.QuoteLine(productId, "Product", "SKU", categoryId,
            java.math.BigDecimal.ONE, java.math.BigDecimal.TEN);
        var quote = com.cashly.cashly_api.quotes.domain.entity.Quote.create(
            new com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber("PRE-2026-" + QUOTE_SEQUENCE.incrementAndGet()),
            customerId, "Customer", "RIF", null, null, null, null, java.util.List.of(quoteLine));
        quotes().save(quote);
        var orderLine = com.cashly.cashly_api.orders.domain.entity.OrderLine.create(productId, "Product", "SKU", categoryId,
            java.math.BigDecimal.ONE, java.math.BigDecimal.TEN, java.util.List.of());
        var order = com.cashly.cashly_api.orders.domain.entity.Order.create(
            com.cashly.cashly_api.orders.domain.valueobjects.OrderNumber.of(2026, 9500),
            customerId, "Customer", "RIF", null, null, null, java.util.List.of(orderLine));
        var repository = new OrderRepositoryAdapter(orderJpa, orderSequences);
        repository.save(order);
        entityManager.flush();
        entityManager.clear();
        assertThat(quotes().findById(quote.getId()).orElseThrow().getLines().getFirst().getCategoryId()).isEqualTo(categoryId);
        assertThat(repository.findById(order.getId()).orElseThrow().getLines().getFirst().getCategoryId()).isEqualTo(categoryId);
    }

    private record QuoteFixture(UUID quoteId, UUID customerId, UUID categoryId, UUID productId, UUID lineId) {}

    private UUID seedQuote() {
        return seedQuoteFixture().quoteId();
    }

    private QuoteFixture seedQuoteFixture() {
        UUID customerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        insertCustomer(customerId);
        insertCategory(categoryId, "Historical");
        insertProduct(productId, categoryId);
        insertQuote(quoteId, customerId);
        insertQuoteLine(lineId, quoteId, productId);
        jdbc.update("insert into quote_line_category_snapshots (quote_line_id,category_id) values (UUID_TO_BIN(?),UUID_TO_BIN(?))", lineId.toString(), categoryId.toString());
        return new QuoteFixture(quoteId, customerId, categoryId, productId, lineId);
    }

    private void assertQuoteSnapshot(UUID quoteId) {
        var saved = quotes().findById(quoteId).orElseThrow();
        assertThat(saved.getLines().getFirst().getCategoryId()).isNotNull();
        assertThat(jdbc.queryForObject("select count(*) from quote_line_category_snapshots s join quote_lines l on l.id=s.quote_line_id where l.quote_id=UUID_TO_BIN(?)", Integer.class, quoteId.toString())).isEqualTo(1);
    }

    private void insertCustomer(UUID id) {
        jdbc.update("insert into customers (id, code, legal_name, type, rif, alias, credit_days, credit_limit, active, created_at, updated_at) values (UUID_TO_BIN(?), ?, ?, 'COMPANY', ?, ?, 0, 0, true, current_timestamp, current_timestamp)",
                id.toString(), Math.abs(id.hashCode()), "Customer " + id, "RIF-" + id, "Alias " + id);
    }

    private void insertCategory(UUID id, String prefix) {
        jdbc.update("insert into categories (id, name, active) values (UUID_TO_BIN(?), ?, true)",
                id.toString(), prefix + " " + id);
    }

    private void insertProduct(UUID id, UUID categoryId) {
        jdbc.update("insert into products (id, code, name, category_id, composite, active, created_at, updated_at) values (UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?), false, true, current_timestamp, current_timestamp)",
                id.toString(), "SKU-" + id, "Product " + id, categoryId.toString());
    }

    private void insertOrder(UUID id, UUID customerId) {
        jdbc.update("insert into orders (id, order_number, customer_id, customer_name, customer_rif, status, subtotal, created_at, updated_at) values (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), 'Customer', 'RIF', 'DRAFT', 10, current_timestamp, current_timestamp)",
                id.toString(), "ORD-" + id.toString().replace("-", "").substring(0, 16), customerId.toString());
    }

    private void insertQuote(UUID id, UUID customerId) {
        jdbc.update("insert into quotes (id, quote_number, customer_id, customer_name, customer_rif, status, subtotal, created_at, updated_at) values (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), 'Customer', 'RIF', 'DRAFT', 10, current_timestamp, current_timestamp)",
                id.toString(), "PRE-2026-" + QUOTE_SEQUENCE.incrementAndGet(), customerId.toString());
    }

    private void insertQuoteLine(UUID id, UUID quoteId, UUID productId) {
        jdbc.update("insert into quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total) values (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), 'Product', 'SKU', 1, 10, 10)",
                id.toString(), quoteId.toString(), productId.toString());
    }

    private void insertLine(UUID id, UUID orderId, UUID productId) {
        jdbc.update("insert into order_lines (id, order_id, product_id, product_name, product_code, quantity, unit_price, line_total) values (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), 'Product', 'SKU', 1, 10, 10)",
                id.toString(), orderId.toString(), productId.toString());
    }
}
