package com.cashly.cashly_api.warehouses.application.usecases;

import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.entities.ProductComponent;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.application.queries.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.warehouses.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.warehouses.application.dto.request.StockBatchItem;
import com.cashly.cashly_api.warehouses.application.dto.request.StockEntryRequest;
import com.cashly.cashly_api.warehouses.application.dto.response.StockMovementResponse;
import com.cashly.cashly_api.warehouses.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
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
@DisplayName("StockEntryUseCase Tests")
class StockEntryUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseStockRepository warehouseStockRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;

    private StockEntryUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new StockEntryUseCase(warehouseRepository, warehouseStockRepository,
                stockMovementRepository, new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(productRepository)));
    }

    @Test
    @DisplayName("adds stock, creates movement, returns response")
    void shouldAddStockAndCreateMovement() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product", false);
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(10);

        StockEntryRequest request = new StockEntryRequest(
                List.of(new StockBatchItem(productId, 5, "Restock"))
        );

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

        List<StockMovementResponse> response = useCase.execute(warehouseId, request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(MovementType.ENTRY, response.get(0).movementType());
        assertEquals(5, response.get(0).quantity());
        assertEquals("PROD-001", response.get(0).productCode());
        assertEquals("Test Product", response.get(0).productName());
        verify(warehouseStockRepository).save(any(WarehouseStock.class));
        verify(warehouseStockRepository).findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        StockEntryRequest request = new StockEntryRequest(
                List.of(new StockBatchItem(productId, 5, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("throws WarehouseNotActiveException when warehouse inactive")
    void shouldThrowWhenWarehouseNotActive() {
        Warehouse warehouse = buildInactiveWarehouse(warehouseId);
        StockEntryRequest request = new StockEntryRequest(
                List.of(new StockBatchItem(productId, 5, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        assertThrows(WarehouseNotActiveException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("throws ProductNotFoundException when product not found")
    void shouldThrowWhenProductNotFound() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        StockEntryRequest request = new StockEntryRequest(
                List.of(new StockBatchItem(productId, 5, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(warehouseId, request));
    }

    @Test
    @DisplayName("composite entry consumes each required component and adds the assembled product")
    void shouldHandleCompositeProductComponents() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        UUID componentId = UUID.randomUUID();
        Product component = buildProduct(componentId, "COMP-001", "Component", false);
        ProductComponent pc = new ProductComponent(UUID.randomUUID(), productId, componentId, 2);
        Product composite = buildCompositeProduct(productId, "COMP-PROD", "Composite", List.of(pc));

        WarehouseStock mainStock = WarehouseStock.create(warehouseId, productId);
        WarehouseStock componentStock = WarehouseStock.create(warehouseId, componentId);
        componentStock.addStock(100);

        StockEntryRequest request = new StockEntryRequest(
                List.of(new StockBatchItem(productId, 3, "Composite entry"))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(composite));
        when(productRepository.findById(componentId)).thenReturn(Optional.of(component));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, componentId))
                .thenReturn(Optional.of(componentStock));
        when(warehouseStockRepository.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(mainStock);
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
        assertEquals(2, response.size());
        assertEquals(94, componentStock.getQuantity());
        assertEquals(3, mainStock.getQuantity());
        assertEquals(MovementType.COMPONENT_EXIT, response.get(0).movementType());
        assertEquals(6, response.get(0).quantity());
        assertEquals(MovementType.ENTRY, response.get(1).movementType());
        assertEquals(3, response.get(1).quantity());
        verify(stockMovementRepository, times(2)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("throws InsufficientStockException when component stock insufficient")
    void shouldThrowWhenComponentStockInsufficient() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        UUID componentId = UUID.randomUUID();
        ProductComponent pc = new ProductComponent(UUID.randomUUID(), productId, componentId, 2);
        Product composite = buildCompositeProduct(productId, "COMP-PROD", "Composite", List.of(pc));

        WarehouseStock componentStock = WarehouseStock.create(warehouseId, componentId);
        componentStock.addStock(1); 

        StockEntryRequest request = new StockEntryRequest(
                List.of(new StockBatchItem(productId, 3, null))
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(productId)).thenReturn(Optional.of(composite));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, componentId))
                .thenReturn(Optional.of(componentStock));

        assertThrows(InsufficientStockException.class, () -> useCase.execute(warehouseId, request));
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

    private Product buildProduct(UUID id, String code, String name, boolean composite) {
        return Product.reconstitute(
                id, code, name, "Desc", null, composite, true, null,
                BigDecimal.ZERO,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Product buildCompositeProduct(UUID id, String code, String name, List<ProductComponent> components) {
        return Product.reconstitute(
                id, code, name, "Desc", null, true, true, null,
                BigDecimal.ZERO,
                components, List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
