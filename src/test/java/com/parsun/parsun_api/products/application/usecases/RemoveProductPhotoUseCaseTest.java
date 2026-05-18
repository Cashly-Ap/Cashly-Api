package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
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
@DisplayName("RemoveProductPhotoUseCase Tests")
class RemoveProductPhotoUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private RemoveProductPhotoUseCase useCase;

    @Test
    void should_RemovePhoto_When_ProductHasPhoto() {
        UUID productId = UUID.randomUUID();
        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                UUID.randomUUID(), false, true, "products/photo.jpg",
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        useCase.execute(productId);

        verify(fileStorageService).delete("products/photo.jpg");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(productId));
        verify(fileStorageService, never()).delete(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_NotCallFileStorage_When_ProductHasNoPhoto() {
        UUID productId = UUID.randomUUID();
        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                UUID.randomUUID(), false, true, null,
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        useCase.execute(productId);

        verify(fileStorageService, never()).delete(any());
        verify(productRepository).save(any(Product.class));
    }
}
