package com.cashly.cashly_api.warehouses.infrastructure;

import com.cashly.cashly_api.warehouses.application.ports.in.StockDeductionApi;
import com.cashly.cashly_api.warehouses.infrastructure.config.StockDeductionConfig;
import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.entities.WarehouseStockEntity;
import com.cashly.cashly_api.warehouses.infrastructure.adapters.out.persistence.repositories.SpringDataWarehouseStockRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {InvoiceInventoryConcurrencyIT.PersistenceConfig.class, StockDeductionConfig.class})
class StockDeductionConcurrencyIT {
    private static final int INITIAL_QUANTITY = 10;
    private static final int REQUESTED_QUANTITY = 7;
    private static final int TIMEOUT_SECONDS = 30;
    private final StockDeductionApi inventory;
    private final SpringDataWarehouseStockRepository stocks;
    private UUID warehouseId;
    private UUID productId;

    @Autowired
    StockDeductionConcurrencyIT(StockDeductionApi inventory, SpringDataWarehouseStockRepository stocks) {
        this.inventory = inventory;
        this.stocks = stocks;
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        InvoiceInventoryConcurrencyIT.properties(registry);
    }

    @AfterAll
    static void stopOwnedDatabase() {
        InvoiceInventoryConcurrencyIT.stopOwnedDatabase();
    }

    @BeforeEach
    void prepareStock() {
        stocks.deleteAll();
        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();
        var stock = new WarehouseStockEntity();
        stock.setId(UUID.randomUUID());
        stock.setWarehouseId(warehouseId);
        stock.setProductId(productId);
        stock.setQuantity(INITIAL_QUANTITY);
        stock.setReserved(0);
        stock.setLastMovementAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());
        stocks.save(stock);
    }

    @Test
    void should_deduct_only_once_when_two_conversions_compete_for_stock() throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> deductAfterSignal(ready, start));
            var second = executor.submit(() -> deductAfterSignal(ready, start));
            assertTrue(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            start.countDown();
            assertNotEquals(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS), second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            var stock = stocks.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow();
            assertEquals(INITIAL_QUANTITY - REQUESTED_QUANTITY, stock.getQuantity());
            assertEquals(0, stock.getReserved());
        } finally {
            start.countDown();
        }
    }

    @Test
    void should_roll_back_the_first_deduction_when_a_later_allocation_is_missing() {
        assertThrows(IllegalStateException.class, () -> inventory.deduct(List.of(
                new StockDeductionApi.Allocation(warehouseId, productId, REQUESTED_QUANTITY),
                new StockDeductionApi.Allocation(warehouseId, UUID.randomUUID(), 1))));
        assertEquals(INITIAL_QUANTITY, stocks.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow().getQuantity());
    }

    private boolean deductAfterSignal(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Stock deduction start timed out");
        }
        try {
            inventory.deduct(List.of(new StockDeductionApi.Allocation(warehouseId, productId, REQUESTED_QUANTITY)));
            return true;
        } catch (IllegalArgumentException | IllegalStateException failure) {
            return false;
        }
    }
}
