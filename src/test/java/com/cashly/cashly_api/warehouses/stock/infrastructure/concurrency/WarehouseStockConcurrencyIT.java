package com.cashly.cashly_api.warehouses.stock.infrastructure.concurrency;

import com.cashly.cashly_api.warehouses.stock.application.ports.out.StockCatalogGateway;
import com.cashly.cashly_api.warehouses.stock.application.dto.response.StockBatchItem;
import com.cashly.cashly_api.warehouses.stock.application.entry.request.StockEntryRequest;
import com.cashly.cashly_api.warehouses.stock.application.transfer.request.TransferStockRequest;
import com.cashly.cashly_api.warehouses.stock.application.entry.use_case.StockEntryUseCase;
import com.cashly.cashly_api.warehouses.stock.application.transfer.use_case.TransferStockUseCase;
import com.cashly.cashly_api.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.repositories.SpringDataWarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InvoiceInventoryConcurrencyIT.PersistenceConfig.class)
class WarehouseStockConcurrencyIT {
    private static final int TIMEOUT_SECONDS = 30;
    private final WarehouseStockRepository stocks;
    private final SpringDataWarehouseStockRepository persistedStocks;
    private final PlatformTransactionManager transactionManager;
    private final WarehouseRepository warehouses = mock(WarehouseRepository.class);
    private final StockMovementRepository movements = mock(StockMovementRepository.class);
    private UUID productId;

    @org.springframework.test.context.DynamicPropertySource
    static void properties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        InvoiceInventoryConcurrencyIT.properties(registry);
    }

    @Autowired
    WarehouseStockConcurrencyIT(WarehouseStockRepository stocks,
                                SpringDataWarehouseStockRepository persistedStocks,
                                PlatformTransactionManager transactionManager) {
        this.stocks = stocks;
        this.persistedStocks = persistedStocks;
        this.transactionManager = transactionManager;
    }

    @BeforeEach
    void initializeProduct() {
        productId = UUID.randomUUID();
        when(movements.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void should_preserve_both_concurrent_entries_when_stock_row_is_missing() throws Exception {
        UUID warehouseId = UUID.randomUUID();
        Warehouse warehouse = activeWarehouse();
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        var catalogRead = new CountDownLatch(2);
        StockCatalogGateway catalog = new StockCatalogGateway() {
            @Override
            public CatalogProduct getProduct(UUID id) {
                catalogRead.countDown();
                try {
                    assertTrue(catalogRead.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                }
                return new CatalogProduct(id, "SKU", "Product", false, List.of());
            }

            @Override
            public Optional<CatalogProduct> findProduct(UUID id) {
                return Optional.of(getProduct(id));
            }
        };
        StockEntryUseCase entry = new StockEntryUseCase(warehouses, stocks, movements, catalog);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit((Callable<Void>) () -> {
                addStock(entry, warehouseId, ready, start);
                return null;
            });
            var second = executor.submit((Callable<Void>) () -> {
                addStock(entry, warehouseId, ready, start);
                return null;
            });
            assertTrue(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            start.countDown();
            first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            start.countDown();
        }
        assertEquals(2, persistedStocks.findByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow().getQuantity());
    }

    @Test
    void should_complete_opposite_transfers_without_deadlock_or_lost_stock() throws Exception {
        UUID firstWarehouse = UUID.randomUUID();
        UUID secondWarehouse = UUID.randomUUID();
        UUID lowerWarehouse = firstWarehouse.compareTo(secondWarehouse) < 0 ? firstWarehouse : secondWarehouse;
        UUID higherWarehouse = firstWarehouse.compareTo(secondWarehouse) < 0 ? secondWarehouse : firstWarehouse;
        persistStock(firstWarehouse, 100);
        persistStock(secondWarehouse, 100);
        Warehouse first = activeWarehouse();
        Warehouse second = activeWarehouse();
        when(warehouses.findById(firstWarehouse)).thenReturn(Optional.of(first));
        when(warehouses.findById(secondWarehouse)).thenReturn(Optional.of(second));
        var catalogReady = new CountDownLatch(2);
        var catalog = catalog(catalogReady);
        TransferStockUseCase transfer = new TransferStockUseCase(warehouses, stocks, movements, catalog);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var lowerToHigher = executor.submit((Callable<Void>) () -> {
                transferAfterSignal(transfer, lowerWarehouse, higherWarehouse, ready, start);
                return null;
            });
            var higherToLower = executor.submit((Callable<Void>) () -> {
                transferAfterSignal(transfer, higherWarehouse, lowerWarehouse, ready, start);
                return null;
            });
            assertTrue(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            start.countDown();
            lowerToHigher.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            higherToLower.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } finally {
            start.countDown();
        }
        assertEquals(100, persistedStocks.findByWarehouseIdAndProductId(firstWarehouse, productId)
                .orElseThrow().getQuantity());
        assertEquals(100, persistedStocks.findByWarehouseIdAndProductId(secondWarehouse, productId)
                .orElseThrow().getQuantity());
    }

    private void addStock(StockEntryUseCase entry, UUID warehouseId, CountDownLatch ready, CountDownLatch start)
            throws Exception {
        ready.countDown();
        assertTrue(start.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> entry.execute(warehouseId,
                new StockEntryRequest(List.of(new StockBatchItem(productId, 1, "Concurrent entry")))));
    }

    private void transferAfterSignal(TransferStockUseCase transfer, UUID source, UUID destination,
                                    CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        assertTrue(start.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> transfer.execute(source,
                new TransferStockRequest(productId, destination, 1, "Opposite transfer")));
    }

    private void persistStock(UUID warehouseId, int quantity) {
        var now = LocalDateTime.now();
        persistedStocks.saveAndFlush(WarehouseStockEntity.fromDomain(new WarehouseStock(UUID.randomUUID(),
                warehouseId, productId, quantity, 0, now, now)));
    }

    private Warehouse activeWarehouse() {
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.isActive()).thenReturn(true);
        return warehouse;
    }

    private StockCatalogGateway catalog(CountDownLatch catalogReady) {
        return new StockCatalogGateway() {
            @Override
            public CatalogProduct getProduct(UUID id) {
                catalogReady.countDown();
                try {
                    assertTrue(catalogReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                }
                return new CatalogProduct(id, "SKU", "Product", false, List.of());
            }

            @Override
            public Optional<CatalogProduct> findProduct(UUID id) {
                return Optional.of(getProduct(id));
            }
        };
    }
}
