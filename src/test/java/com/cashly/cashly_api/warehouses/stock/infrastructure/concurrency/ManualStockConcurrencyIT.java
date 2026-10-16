package com.cashly.cashly_api.warehouses.stock.infrastructure.concurrency;

import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.Product;
import com.cashly.cashly_api.products.domain.ProductRepository;
import com.cashly.cashly_api.products.application.queries.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.warehouses.stock.application.dto.StockBatchItem;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryRequest;
import com.cashly.cashly_api.warehouses.stock.application.exit.StockExitRequest;
import com.cashly.cashly_api.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.warehouses.stock.application.entry.StockEntryUseCase;
import com.cashly.cashly_api.warehouses.stock.application.exit.StockExitUseCase;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InvoiceInventoryConcurrencyIT.PersistenceConfig.class)
class ManualStockConcurrencyIT {
    private static final int INITIAL_QUANTITY = 10;
    private static final int REQUESTED_QUANTITY = 7;
    private static final int TIMEOUT_SECONDS = 30;
    private static final int BLOCK_CHECK_MILLISECONDS = 200;
    private final WarehouseStockRepository stocks;
    private final SpringDataWarehouseStockRepository persistedStocks;
    private final PlatformTransactionManager transactionManager;
    private final WarehouseRepository warehouses = mock(WarehouseRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final StockMovementRepository movements = mock(StockMovementRepository.class);
    private UUID warehouseId;
    private UUID productId;

    @org.springframework.test.context.DynamicPropertySource
    static void properties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        InvoiceInventoryConcurrencyIT.properties(registry);
    }

    @Autowired
    ManualStockConcurrencyIT(WarehouseStockRepository stocks, SpringDataWarehouseStockRepository persistedStocks,
                             PlatformTransactionManager transactionManager) {
        this.stocks = stocks;
        this.persistedStocks = persistedStocks;
        this.transactionManager = transactionManager;
    }
    @BeforeEach
    void givenExistingStockAndCatalogProduct() {
        warehouseId = UUID.randomUUID();
        productId = UUID.randomUUID();
        var now = LocalDateTime.now();
        persistedStocks.saveAndFlush(WarehouseStockEntity.fromDomain(new WarehouseStock(UUID.randomUUID(),
                warehouseId, productId, INITIAL_QUANTITY, 0, now, now)));
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.isActive()).thenReturn(true);
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(productId);
        when(products.findById(productId)).thenReturn(Optional.of(product));
        when(movements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldWaitForCompetingMutationAndRejectExitAgainstUpdatedStock() throws Exception {
        StockExitUseCase exit = new StockExitUseCase(warehouses, stocks, movements, new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(products)));
        var productRead = new CountDownLatch(1);
        Product product = products.findById(productId).orElseThrow();
        when(products.findById(productId)).thenAnswer(invocation -> {
            productRead.countDown();
            return Optional.of(product);
        });
        try (var executor = Executors.newSingleThreadExecutor()) {
            var task = new java.util.concurrent.atomic.AtomicReference<java.util.concurrent.Future<Throwable>>();
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                WarehouseStock locked = stocks.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId).orElseThrow();
                task.set(executor.submit(() -> {
                    try {
                        new TransactionTemplate(transactionManager).executeWithoutResult(workerStatus -> exit.execute(warehouseId,
                                new StockExitRequest(List.of(new StockBatchItem(productId, REQUESTED_QUANTITY, "Exit")))));
                        return null;
                    } catch (Throwable failure) {
                        return failure;
                    }
                }));
                try {
                    assertTrue(productRead.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    assertThrows(TimeoutException.class, () -> task.get().get(BLOCK_CHECK_MILLISECONDS, TimeUnit.MILLISECONDS));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(interrupted);
                }
                locked.removeStock(REQUESTED_QUANTITY);
                stocks.save(locked);
            });
            assertInstanceOf(InsufficientStockException.class, task.get().get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(INITIAL_QUANTITY - REQUESTED_QUANTITY,
                    persistedStocks.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow().getQuantity());
            verify(movements, never()).save(any());
        }
    }

    @Test
    void shouldRollBackEarlierEntryWhenLaterCatalogProductIsMissing() {
        UUID missingProductId = UUID.randomUUID();
        StockEntryUseCase entry = new StockEntryUseCase(warehouses, stocks, movements, new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(products)));
        assertThrows(ProductNotFoundException.class, () -> new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> entry.execute(warehouseId, new StockEntryRequest(List.of(
                        new StockBatchItem(productId, REQUESTED_QUANTITY, "Entry"),
                        new StockBatchItem(missingProductId, 1, "Missing"))))));
        assertEquals(INITIAL_QUANTITY,
                persistedStocks.findByWarehouseIdAndProductId(warehouseId, productId).orElseThrow().getQuantity());
        verify(movements).save(any(StockMovement.class));
    }
}
