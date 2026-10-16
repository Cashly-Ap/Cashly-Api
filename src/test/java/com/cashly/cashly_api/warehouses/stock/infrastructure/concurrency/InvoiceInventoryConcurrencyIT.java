package com.cashly.cashly_api.warehouses.stock.infrastructure.concurrency;

import com.cashly.cashly_api.warehouses.stock.application.ports.in.InvoiceInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.InvoiceStockCommand.Allocation;
import com.cashly.cashly_api.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.warehouses.stock.application.invoices.InvoiceInventoryService;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.adapters.JpaWarehouseStockRepository;
import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.repositories.SpringDataWarehouseStockRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.junit.jupiter.api.AfterAll;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InvoiceInventoryConcurrencyIT.PersistenceConfig.class)
class InvoiceInventoryConcurrencyIT {
    private static final int STOCK_QUANTITY = 10;
    private static final int REQUESTED_QUANTITY = 7;
    private static final int TIMEOUT_SECONDS = 30;

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("invoice_inventory_test").withUsername("inventory_test")
            .withPassword("inventory_test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String externalUrl = System.getProperty("inventory.test.url");
        if (externalUrl != null) {
            registry.add("inventory.test.url", () -> externalUrl);
            registry.add("inventory.test.username", () -> System.getProperty("inventory.test.username", "inventory_test"));
            registry.add("inventory.test.password", () -> System.getProperty("inventory.test.password", "inventory_test"));
            return;
        }
        MYSQL.start();
        registry.add("inventory.test.url", MYSQL::getJdbcUrl);
        registry.add("inventory.test.username", MYSQL::getUsername);
        registry.add("inventory.test.password", MYSQL::getPassword);
    }

    @AfterAll
    static void stopOwnedDatabase() {
        if (MYSQL.isRunning()) {
            MYSQL.stop();
        }
    }

    private final InvoiceInventoryApi inventory;
    private final SpringDataWarehouseStockRepository stocks;
    private final PlatformTransactionManager transactionManager;
    private UUID warehouseId;
    private UUID productId;

    @Autowired
    InvoiceInventoryConcurrencyIT(InvoiceInventoryApi inventory, SpringDataWarehouseStockRepository stocks,
                                  PlatformTransactionManager transactionManager) {
        this.inventory = inventory;
        this.stocks = stocks;
        this.transactionManager = transactionManager;
    }

    @BeforeEach
    void given_ten_unreserved_units() {
        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();
        var now = LocalDateTime.now();
        stocks.saveAndFlush(WarehouseStockEntity.fromDomain(new WarehouseStock(UUID.randomUUID(),
                warehouseId, productId, STOCK_QUANTITY, 0, now, now)));
    }

    @Test
    void should_allow_only_one_of_two_concurrent_reservations() throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> reserveAfterSignal(ready, start));
            var second = executor.submit(() -> reserveAfterSignal(ready, start));
            assertTrue(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "Reservation workers did not start");
            start.countDown();
            var outcomes = List.of(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(1, outcomes.stream().filter(ReservationOutcome::successful).count());
            assertEquals(1, outcomes.stream().filter(result -> !result.successful()).count());
            assertTrue(outcomes.stream().filter(result -> !result.successful())
                    .allMatch(result -> result.failure().startsWith("Insufficient available stock:")));
            WarehouseStockEntity saved = stocks.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow();
            assertEquals(REQUESTED_QUANTITY, saved.getReserved());
            assertEquals(STOCK_QUANTITY, saved.getQuantity());
        } finally {
            start.countDown();
        }
    }

    @Test
    void should_roll_back_prior_reservation_when_later_allocation_is_missing() {
        UUID missingProductId = UUID.randomUUID();
        assertThrows(ProductNotInWarehouseException.class, () -> inventory.reserveAllocations(List.of(
                new Allocation(warehouseId, productId, REQUESTED_QUANTITY),
                new Allocation(warehouseId, missingProductId, 1))));
        WarehouseStockEntity saved = stocks.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow();
        assertEquals(0, saved.getReserved());
        assertEquals(STOCK_QUANTITY, saved.getQuantity());
    }

    private ReservationOutcome reserveAfterSignal(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Reservation start timed out");
        }
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                    inventory.reserveAllocations(List.of(new Allocation(warehouseId, productId, REQUESTED_QUANTITY))));
            return new ReservationOutcome(true, null);
        } catch (IllegalStateException exception) {
            return new ReservationOutcome(false, exception.getMessage());
        }
    }

    private record ReservationOutcome(boolean successful, String failure) {}

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = SpringDataWarehouseStockRepository.class)
    static class PersistenceConfig {
        @Bean
        DataSource dataSource(Environment environment) {
            return new DriverManagerDataSource(environment.getRequiredProperty("inventory.test.url"),
                    environment.getRequiredProperty("inventory.test.username"),
                    environment.getRequiredProperty("inventory.test.password"));
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(WarehouseStockEntity.class.getPackageName());
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }

        @Bean
        WarehouseStockRepository stockRepository(SpringDataWarehouseStockRepository stocks) {
            return new JpaWarehouseStockRepository(stocks);
        }

        @Bean
        InvoiceInventoryApi invoiceInventoryApi(WarehouseStockRepository stocks) {
            return new InvoiceInventoryService(stocks, mock(StockMovementRepository.class));
        }
    }
}


