package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
@DisplayName("DeleteProductUseCase Tests")
class DeleteProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @InjectMocks
    private DeleteProductUseCase useCase;

    @Test
    void should_DeleteProduct_When_ProductExists() {
        UUID id = UUID.randomUUID();
        Product product = Product.reconstitute(id, "PROD-001", "Test Product", "Description",
                UUID.randomUUID(), false, true, null,
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        useCase.execute(id);

        verify(stockMovementRepository).deleteByProductId(id);
        verify(warehouseStockRepository).deleteByProductId(id);
        verify(productRepository).deleteComponentReferencesByProductId(id);
        verify(productRepository).delete(id);
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(id));
        verify(stockMovementRepository, never()).deleteByProductId(any());
        verify(warehouseStockRepository, never()).deleteByProductId(any());
        verify(productRepository, never()).delete(any());
    }
}
