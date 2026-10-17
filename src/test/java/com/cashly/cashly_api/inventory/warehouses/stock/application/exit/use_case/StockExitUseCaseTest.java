package com.cashly.cashly_api.inventory.warehouses.stock.application.exit.use_case;

import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockBatchItem;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exit.request.StockExitRequest;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
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
@DisplayName("StockExitUseCase Tests")
class StockExitUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseStockRepository warehouseStockRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;

    private StockExitUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new StockExitUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(productRepository)));
    }

    @Test
    @DisplayName("removes stock, creates movement, returns response")
    void shouldRemoveStockAndCreateMovement() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(100);

        StockExitRequest request = new StockExitRequest(
                List.of(new StockBatchItem(productId, 10, "Sale"))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            return new StockMovement(UUID.randomUUID(), m.getWarehouseId(), m.getProductId(),
                    m.getMovementType(), m.getQuantity(), m.getPreviousStock(), m.getNewStock(),
                    m.getOriginMovementId(), m.getTransferBatchId(), m.getReferenceType(),
                    m.getReferenceId(), m.getReferenceNumber(), m.getNotes(), m.getCreatedAt());
        });

        List<StockMovementResponse> response = useCase.execute(warehouseId, request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(MovementType.EXIT, response.get(0).movementType());
        assertEquals(10, response.get(0).quantity());
        assertEquals("PROD-001", response.get(0).productCode());
        verify(warehouseStockRepository).save(any(WarehouseStock.class));
        verify(warehouseStockRepository, atLeastOnce()).findByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        StockExitRequest request = new StockExitRequest(
                List.of(new StockBatchItem(productId, 10, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("throws ProductNotInWarehouseException when product not in warehouse")
    void shouldThrowWhenProductNotInWarehouse() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");

        StockExitRequest request = new StockExitRequest(
                List.of(new StockBatchItem(productId, 10, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotInWarehouseException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("throws InsufficientStockException when not enough stock")
    void shouldThrowWhenInsufficientStock() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(5); 

        StockExitRequest request = new StockExitRequest(
                List.of(new StockBatchItem(productId, 10, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));

        assertThrows(InsufficientStockException.class, () -> useCase.execute(warehouseId, request));
    }

    private Warehouse buildActiveWarehouse(UUID id) {
        return Warehouse.reconstitute(
                id, "WH-001", "Main", "Addr", "State", "Mun", "10001",
                true, LocalDateTime.now(), LocalDateTime.now()
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
