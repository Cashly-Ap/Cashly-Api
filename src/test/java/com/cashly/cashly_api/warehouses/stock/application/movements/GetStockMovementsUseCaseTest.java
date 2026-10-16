package com.cashly.cashly_api.warehouses.stock.application.movements;

import com.cashly.cashly_api.products.domain.Product;
import com.cashly.cashly_api.products.domain.ProductRepository;
import com.cashly.cashly_api.products.application.queries.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.application.movements.StockMovementResponse;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.management.domain.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.management.domain.WarehouseRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStockMovementsUseCase Tests")
class GetStockMovementsUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ProductRepository productRepository;

    private GetStockMovementsUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetStockMovementsUseCase(warehouseRepository, stockMovementRepository,
                new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(productRepository)));
    }

    @Test
    @DisplayName("returns paginated movements with product info")
    void shouldReturnPaginatedMovementsWithProductInfo() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        StockMovement movement = StockMovement.create(
                warehouseId, productId, MovementType.ENTRY, 10, 0, 10, null, null, "Entry"
        );

        PageResponse<StockMovement> page = new PageResponse<>(
                List.of(movement), 1L, 1, 10, 0, true, true, false
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseId(warehouseId, 0, 10)).thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        PageResponse<StockMovementResponse> response = useCase.execute(warehouseId, null, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("PROD-001", response.content().get(0).productCode());
        assertEquals("Test Product", response.content().get(0).productName());
        assertEquals(MovementType.ENTRY, response.content().get(0).movementType());
        verify(warehouseRepository).findById(warehouseId);
        verify(stockMovementRepository).findByWarehouseId(warehouseId, 0, 10);
    }

    @Test
    @DisplayName("with productId filter: calls findByWarehouseIdAndProductId")
    void shouldCallFindByWarehouseIdAndProductIdWhenProductIdProvided() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product");
        StockMovement movement = StockMovement.create(
                warehouseId, productId, MovementType.EXIT, 5, 100, 95, null, null, "Exit"
        );

        PageResponse<StockMovement> page = new PageResponse<>(
                List.of(movement), 1L, 1, 10, 0, true, true, false
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseIdAndProductId(warehouseId, productId, 0, 10))
                .thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        PageResponse<StockMovementResponse> response = useCase.execute(warehouseId, productId, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        verify(stockMovementRepository).findByWarehouseIdAndProductId(warehouseId, productId, 0, 10);
        verify(stockMovementRepository, never()).findByWarehouseId(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("without productId: calls findByWarehouseId")
    void shouldCallFindByWarehouseIdWhenNoProductId() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);

        PageResponse<StockMovement> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseId(warehouseId, 0, 10)).thenReturn(page);

        PageResponse<StockMovementResponse> response = useCase.execute(warehouseId, null, null, 0, 10);

        assertNotNull(response);
        verify(stockMovementRepository).findByWarehouseId(warehouseId, 0, 10);
        verify(stockMovementRepository, never()).findByWarehouseIdAndProductId(any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("with movement type filter: delegates filtering before pagination")
    void shouldFilterByMovementTypeBeforePagination() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        PageResponse<StockMovement> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseIdAndMovementType(
                warehouseId, MovementType.EXIT, 0, 10)).thenReturn(page);

        useCase.execute(warehouseId, null, MovementType.EXIT, 0, 10);

        verify(stockMovementRepository).findByWarehouseIdAndMovementType(
                warehouseId, MovementType.EXIT, 0, 10);
        verify(stockMovementRepository, never()).findByWarehouseId(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("with product and movement type filters: combines both filters")
    void shouldCombineProductAndMovementTypeFilters() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        PageResponse<StockMovement> page = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(stockMovementRepository.findByWarehouseIdAndProductIdAndMovementType(
                warehouseId, productId, MovementType.ENTRY, 0, 10)).thenReturn(page);

        useCase.execute(warehouseId, productId, MovementType.ENTRY, 0, 10);

        verify(stockMovementRepository).findByWarehouseIdAndProductIdAndMovementType(
                warehouseId, productId, MovementType.ENTRY, 0, 10);
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class,
                () -> useCase.execute(warehouseId, null, null, 0, 10));

        verify(warehouseRepository).findById(warehouseId);
        verify(stockMovementRepository, never()).findByWarehouseId(any(), anyInt(), anyInt());
    }

    @Test
    void shouldPreserveUnknownProductLabelsInHistoricalMovements() {
        StockMovement movement = StockMovement.create(warehouseId, productId, MovementType.ENTRY,
                10, 0, 10, null, null, "Historical");
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(buildActiveWarehouse(warehouseId)));
        when(stockMovementRepository.findByWarehouseId(warehouseId, 0, 10))
                .thenReturn(new PageResponse<>(List.of(movement), 1L, 1, 10, 0, true, true, false));
        var response = useCase.execute(warehouseId, null, null, 0, 10);
        assertEquals("UNKNOWN", response.content().getFirst().productCode());
        assertEquals("Unknown", response.content().getFirst().productName());
        assertEquals(MovementType.ENTRY, response.content().getFirst().movementType());
        assertEquals(1L, response.totalElements());
        assertEquals(10, response.size());
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
