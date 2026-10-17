package com.cashly.cashly_api.catalog.products.application.photos.use_case;

import com.cashly.cashly_api.catalog.categories.domain.entity.Category;
import com.cashly.cashly_api.catalog.categories.domain.repository.CategoryRepository;
import com.cashly.cashly_api.catalog.price_types.domain.entity.PriceType;
import com.cashly.cashly_api.catalog.price_types.domain.repository.PriceTypeRepository;
import com.cashly.cashly_api.catalog.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.catalog.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.catalog.products.domain.entity.Product;
import com.cashly.cashly_api.catalog.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.catalog.products.application.ports.out.ProductFileStorageGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import com.cashly.cashly_api.catalog.products.infrastructure.integration.LocalProductCategoryGatewayAdapter;
import com.cashly.cashly_api.catalog.products.infrastructure.integration.LocalProductPriceTypeGatewayAdapter;
import com.cashly.cashly_api.catalog.categories.application.queries.services.RepositoryProductCategoryQuery;
import com.cashly.cashly_api.catalog.price_types.application.queries.services.RepositoryProductPriceTypeQuery;
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
    private ProductFileStorageGateway fileStorageService;

    @Mock
    private ProductMapper productMapper;

    private UploadProductPhotoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UploadProductPhotoUseCase(productRepository, new LocalProductCategoryGatewayAdapter(new RepositoryProductCategoryQuery(categoryRepository)), new LocalProductPriceTypeGatewayAdapter(new RepositoryProductPriceTypeQuery(priceTypeRepository)), fileStorageService, productMapper);
    }

    @Test
    void should_UploadPhoto_When_ProductExists() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        Product savedProduct = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, "products/image.jpg",
                new BigDecimal("5.00"),
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
                new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        Product savedProduct = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                categoryId, false, true, "products/new-photo.jpg",
                new BigDecimal("5.00"),
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
        verify(fileStorageService).remove("products/old-photo.jpg");
        verify(fileStorageService).store(eq("products/new-photo.jpg"), any(InputStream.class), eq("image/jpeg"));
    }

    @Test
    void should_PropagateStorageFailure_WithoutSavingProduct() {
        UUID productId = UUID.randomUUID();
        Product product = Product.reconstitute(productId, "PROD-001", "Test Product", "Description",
                UUID.randomUUID(), false, true, null, new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());
        var failure = new IllegalStateException("Storage unavailable");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(fileStorageService.store(anyString(), any(InputStream.class), anyString())).thenThrow(failure);

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> useCase.execute(productId, "image.jpg", new ByteArrayInputStream(new byte[0]), "image/jpeg"));

        assertSame(failure, thrown);
        verify(productRepository, never()).save(any());
    }
}
