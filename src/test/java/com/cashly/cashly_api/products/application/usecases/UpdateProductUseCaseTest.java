package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.price_types.domain.entities.PriceType;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.products.application.dto.request.ProductPriceRequest;
import com.cashly.cashly_api.products.application.dto.request.UpdateProductRequest;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.cashly.cashly_api.products.application.exceptions.DuplicateProductComponentException;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import com.cashly.cashly_api.products.infrastructure.integration.LocalProductCategoryGatewayAdapter;
import com.cashly.cashly_api.products.infrastructure.integration.LocalProductPriceTypeGatewayAdapter;
import com.cashly.cashly_api.categories.application.queries.RepositoryProductCategoryQuery;
import com.cashly.cashly_api.price_types.application.queries.RepositoryProductPriceTypeQuery;
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
@DisplayName("UpdateProductUseCase Tests")
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @Mock
    private ProductMapper productMapper;

    private UpdateProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductUseCase(productRepository, new LocalProductCategoryGatewayAdapter(new RepositoryProductCategoryQuery(categoryRepository)), new LocalProductPriceTypeGatewayAdapter(new RepositoryProductPriceTypeQuery(priceTypeRepository)), productMapper);
    }

    @Test
    void should_UpdateProduct_When_ValidRequest() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Updated Name", "Updated desc", categoryId,
                new BigDecimal("20.00"),
                false, null, null, true);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Old Name", "Old desc",
                categoryId, false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        Product savedProduct = Product.reconstitute(id, "PROD-001", "Updated Name", "Updated desc",
                categoryId, false, true, null,
                new BigDecimal("10.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null, false, null, null, true);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(id, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_CategoryNotFound() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null, false, null, null, true);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(id, request));
    }

    @Test
    void should_MakeProductComposite_When_RequestCompositeTrue() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null,
                true, List.of(), null, null);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Cat");

        Product savedProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(id, request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void should_SetComponents_When_CompositeWithComponents() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UpdateProductRequest.ComponentRequest compReq = new UpdateProductRequest.ComponentRequest(componentId, 3);

        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null,
                true, List.of(compReq), null, null);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Product componentProduct = Product.reconstitute(componentId, "COMP-001", "Component",
                "desc", categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Cat");

        Product savedProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findById(componentId)).thenReturn(Optional.of(componentProduct));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(id, request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void should_SetProductPrices_When_ProductPricesProvided() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();

        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null,
                false, null,
                List.of(new ProductPriceRequest(priceTypeId, new BigDecimal("30.00"), null)),
                null);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Cat");

        Product savedProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(priceTypeRepository.findById(priceTypeId)).thenReturn(Optional.of(mock(PriceType.class)));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(id, request);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void should_ThrowException_When_DuplicateComponent() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UpdateProductRequest.ComponentRequest compReq = new UpdateProductRequest.ComponentRequest(componentId, 2);

        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null,
                true, List.of(compReq, compReq), null, null);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Product componentProduct = Product.reconstitute(componentId, "COMP-001", "Component",
                "desc", categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(productRepository.findById(componentId)).thenReturn(Optional.of(componentProduct));

        assertThrows(DuplicateProductComponentException.class, () -> useCase.execute(id, request));
    }

    @Test
    void should_ThrowException_When_ComponentIsComposite() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UpdateProductRequest.ComponentRequest compReq = new UpdateProductRequest.ComponentRequest(componentId, 2);

        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null,
                true, List.of(compReq), null, null);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Product compositeComponent = Product.reconstitute(componentId, "COMP-001", "Component",
                "desc", categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(productRepository.findById(componentId)).thenReturn(Optional.of(compositeComponent));

        assertThrows(CompositeProductCannotBeComponentException.class, () -> useCase.execute(id, request));
    }

    @Test
    void should_ThrowException_When_PriceTypeNotFound() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();

        UpdateProductRequest request = new UpdateProductRequest(
                "Name", "desc", categoryId, null,
                false, null,
                List.of(new ProductPriceRequest(priceTypeId, new BigDecimal("10.00"), null)),
                null);

        Product existingProduct = Product.reconstitute(id, "PROD-001", "Name", "desc",
                categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(priceTypeRepository.findById(priceTypeId)).thenReturn(Optional.empty());

        assertThrows(PriceTypeNotFoundException.class, () -> useCase.execute(id, request));
    }
}
