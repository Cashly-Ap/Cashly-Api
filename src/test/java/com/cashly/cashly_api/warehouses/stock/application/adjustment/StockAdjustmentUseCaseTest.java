package com.cashly.cashly_api.warehouses.stock.application.adjustment;

import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.Product;
import com.cashly.cashly_api.products.domain.ProductRepository;
import com.cashly.cashly_api.products.application.queries.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.warehouses.stock.application.adjustment.StockAdjustmentRequest;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockAdjustmentUseCase Tests")
class StockAdjustmentUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseStockRepository warehouseStockRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;

    private StockAdjustmentUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new StockAdjustmentUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(productRepository)));
    }

    @Test
    @DisplayName("adjusts stock, creates ADJUSTMENT movement")
    void shouldAdjustStockAndCreateMovement() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(50);

        StockAdjustmentRequest request = new StockAdjustmentRequest(productId, 75, "Inventory count");

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(stock);
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            return new StockMovement(UUID.randomUUID(), m.getWarehouseId(), m.getProductId(),
                    m.getMovementType(), m.getQuantity(), m.getPreviousStock(), m.getNewStock(),
                    m.getOriginMovementId(), m.getTransferBatchId(), m.getReferenceType(),
                    m.getReferenceId(), m.getReferenceNumber(), m.getNotes(), m.getCreatedAt());
        });

        StockMovementResponse response = useCase.execute(warehouseId, request);

        assertNotNull(response);
        assertEquals(MovementType.ADJUSTMENT, response.movementType());
        assertEquals(75, response.newStock());
        assertEquals("PROD-001", response.productCode());
        verify(warehouseStockRepository).save(any(WarehouseStock.class));
        verify(warehouseStockRepository).findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        StockAdjustmentRequest request = new StockAdjustmentRequest(productId, 50, null);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("throws WarehouseNotActiveException when warehouse inactive")
    void shouldThrowWhenWarehouseNotActive() {
        Warehouse warehouse = buildInactiveWarehouse(warehouseId);
        StockAdjustmentRequest request = new StockAdjustmentRequest(productId, 50, null);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        assertThrows(WarehouseNotActiveException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("throws ProductNotFoundException when product not found")
    void shouldThrowWhenProductNotFound() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        StockAdjustmentRequest request = new StockAdjustmentRequest(productId, 50, null);

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("creates stock if not exists (orElseGet)")
    void shouldCreateStockWhenNotExists() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");

        StockAdjustmentRequest request = new StockAdjustmentRequest(productId, 30, "Initial stock");

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(WarehouseStock.create(warehouseId, productId));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            return new StockMovement(UUID.randomUUID(), m.getWarehouseId(), m.getProductId(),
                    m.getMovementType(), m.getQuantity(), m.getPreviousStock(), m.getNewStock(),
                    m.getOriginMovementId(), m.getTransferBatchId(), m.getReferenceType(),
                    m.getReferenceId(), m.getReferenceNumber(), m.getNotes(), m.getCreatedAt());
        });

        StockMovementResponse response = useCase.execute(warehouseId, request);

        assertNotNull(response);
        assertEquals(MovementType.ADJUSTMENT, response.movementType());
        assertEquals(30, response.newStock());
        verify(warehouseStockRepository).save(any(WarehouseStock.class));
        verify(warehouseStockRepository).findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
    }

    private Warehouse buildActiveWarehouse(UUID id) {
        return Warehouse.reconstitute(
                id, "WH-001", "Main", "Addr", "State", "Mun", "10001",
                true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Warehouse buildInactiveWarehouse(UUID id) {
        return Warehouse.reconstitute(
                id, "WH-001", "Main", "Addr", "State", "Mun", "10001",
                false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Product buildProduct(UUID id, String code, String name) {
        return Product.reconstitute(
                id, code, name, "Desc", null, false, true, null,
                BigDecimal.ZERO,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
