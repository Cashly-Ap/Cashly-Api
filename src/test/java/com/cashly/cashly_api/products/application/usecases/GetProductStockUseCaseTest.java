package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.products.application.dto.response.ProductStockByWarehouseResponse;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.cashly.cashly_api.products.infrastructure.integration.LocalProductWarehouseStockGatewayAdapter;
import com.cashly.cashly_api.warehouses.application.services.RepositoryProductStockQuery;
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
@DisplayName("GetProductStockUseCase Tests")
class GetProductStockUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private GetProductStockUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductStockUseCase(productRepository,
                new LocalProductWarehouseStockGatewayAdapter(
                        new RepositoryProductStockQuery(
                                warehouseStockRepository, warehouseRepository)));
    }

    @Test
    void should_ReturnStockByWarehouse_When_ProductExists() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                UUID.randomUUID(), false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), now, now);

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId,
                100, 0, now, now);

        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getName()).thenReturn("Main Warehouse");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByProductId(productId)).thenReturn(List.of(stock));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        List<ProductStockByWarehouseResponse> result = useCase.execute(productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Main Warehouse", result.get(0).warehouseName());
        assertEquals(100, result.get(0).quantity());
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(productId));
        verify(warehouseStockRepository, never()).findByProductId(any());
    }

    @Test
    void should_UseUnknownName_When_WarehouseNotFound() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                UUID.randomUUID(), false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), now, now);

        WarehouseStock stock = new WarehouseStock(UUID.randomUUID(), warehouseId, productId,
                50, 0, now, now);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(warehouseStockRepository.findByProductId(productId)).thenReturn(List.of(stock));
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        List<ProductStockByWarehouseResponse> result = useCase.execute(productId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).warehouseName());
    }
}
