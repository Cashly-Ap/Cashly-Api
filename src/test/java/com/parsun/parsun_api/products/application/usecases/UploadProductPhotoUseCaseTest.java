package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.products.application.mappers.ProductMapper;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadProductPhotoUseCase Tests")
class UploadProductPhotoUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private UploadProductPhotoUseCase useCase;

    @Test
    void should_UploadPhoto_When_ProductExists() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, null,
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        Product savedProduct = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, "products/image.jpg",
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(fileStorageService.store(eq("products/image.jpg"), any(InputStream.class), eq("image/jpeg")))
                .thenReturn("products/image.jpg");
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(productId, "image.jpg", inputStream, "image/jpeg");

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(fileStorageService).store(eq("products/image.jpg"), any(InputStream.class), eq("image/jpeg"));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID productId = UUID.randomUUID();
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () ->
                useCase.execute(productId, "image.jpg", inputStream, "image/jpeg"));
        verify(fileStorageService, never()).store(any(), any(), any());
    }

    @Test
    void should_DeleteOldPhoto_When_ProductHasExistingPhoto() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, "products/old-photo.jpg",
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        Product savedProduct = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, "products/new-photo.jpg",
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(fileStorageService.store(eq("products/new-photo.jpg"), any(InputStream.class), eq("image/jpeg")))
                .thenReturn("products/new-photo.jpg");
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(productId, "new-photo.jpg", inputStream, "image/jpeg");

        assertNotNull(result);
        verify(fileStorageService).delete("products/old-photo.jpg");
        verify(fileStorageService).store(eq("products/new-photo.jpg"), any(InputStream.class), eq("image/jpeg"));
    }
}
