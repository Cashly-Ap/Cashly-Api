package com.cashly.cashly_api.warehouses.stock.application.queries.use_case;

import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.queries.services.RepositoryStockCatalogQuery;
import com.cashly.cashly_api.warehouses.stock.infrastructure.integration.LocalStockCatalogGatewayAdapter;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.application.dto.response.WarehouseStockResponse;
import com.cashly.cashly_api.warehouses.management.application.exceptions.WarehouseNotFoundException;
import com.cashly.cashly_api.warehouses.management.domain.entity.Warehouse;
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
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
@DisplayName("GetWarehouseStockUseCase Tests")
class GetWarehouseStockUseCaseTest {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseStockRepository warehouseStockRepository;
    @Mock
    private ProductRepository productRepository;

    private GetWarehouseStockUseCase useCase;

    private final UUID warehouseId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetWarehouseStockUseCase(warehouseRepository, warehouseStockRepository,
                new LocalStockCatalogGatewayAdapter(new RepositoryStockCatalogQuery(productRepository)));
    }

    @Test
    @DisplayName("returns paginated stock with product names")
    void shouldReturnPaginatedStockWithProductInfo() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        Product product = buildProduct(productId, "PROD-001", "Test Product", true);
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(50);

        PageResponse<WarehouseStock> page = new PageResponse<>(
                List.of(stock), 1L, 1, 10, 0, true, true, false
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseStockRepository.findByWarehouseIdPaged(warehouseId, 0, 10)).thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        PageResponse<WarehouseStockResponse> response = useCase.execute(warehouseId, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("PROD-001", response.content().get(0).productCode());
        assertEquals("Test Product", response.content().get(0).productName());
        assertTrue(response.content().get(0).composite());
        assertEquals(50, response.content().get(0).quantity());
        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseStockRepository).findByWarehouseIdPaged(warehouseId, 0, 10);
    }

    @Test
    @DisplayName("throws WarehouseNotFoundException when warehouse not found")
    void shouldThrowWhenWarehouseNotFound() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(WarehouseNotFoundException.class, () -> useCase.execute(warehouseId, 0, 10));

        verify(warehouseRepository).findById(warehouseId);
        verify(warehouseStockRepository, never()).findByWarehouseIdPaged(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("uses UNKNOWN/Unknown when product not found")
    void shouldUseUnknownWhenProductNotFound() {
        Warehouse warehouse = buildActiveWarehouse(warehouseId);
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(25);

        PageResponse<WarehouseStock> page = new PageResponse<>(
                List.of(stock), 1L, 1, 10, 0, true, true, false
        );

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(warehouseStockRepository.findByWarehouseIdPaged(warehouseId, 0, 10)).thenReturn(page);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        PageResponse<WarehouseStockResponse> response = useCase.execute(warehouseId, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.content().size());
        assertEquals("UNKNOWN", response.content().get(0).productCode());
        assertEquals("Unknown", response.content().get(0).productName());
    }

    private Warehouse buildActiveWarehouse(UUID id) {
        return Warehouse.reconstitute(
                id, "WH-001", "Main", "Addr", "State", "Mun", "10001",
                true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private Product buildProduct(UUID id, String code, String name, boolean composite) {
        return Product.reconstitute(
                id, code, name, "Desc", null, composite, true, null,
                BigDecimal.ZERO,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
