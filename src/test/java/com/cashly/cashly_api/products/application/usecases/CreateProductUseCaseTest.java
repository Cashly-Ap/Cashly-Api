package com.cashly.cashly_api.products.application.usecases;

import com.cashly.cashly_api.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.categories.domain.entities.Category;
import com.cashly.cashly_api.categories.domain.repositories.CategoryRepository;
import com.cashly.cashly_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.price_types.domain.entities.PriceType;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.products.application.dto.request.CreateProductRequest;
import com.cashly.cashly_api.products.application.dto.request.ProductPriceRequest;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.cashly.cashly_api.products.application.exceptions.DuplicateProductComponentException;
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
@DisplayName("CreateProductUseCase Tests")
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @Mock
    private ProductMapper productMapper;

    private CreateProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateProductUseCase(productRepository, new LocalProductCategoryGatewayAdapter(new RepositoryProductCategoryQuery(categoryRepository)), new LocalProductPriceTypeGatewayAdapter(new RepositoryProductPriceTypeQuery(priceTypeRepository)), productMapper);
    }

    @Test
    void should_CreateProduct_When_ValidRequest() {
        UUID categoryId = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                null, "Test Product", "Description", categoryId, false,
                new BigDecimal("10.00"), null,
                List.of(new ProductPriceRequest(priceTypeId, new BigDecimal("25.00"), null)));

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        Product savedProduct = Product.reconstitute(UUID.randomUUID(), "PROD-001", "Test Product",
                "Description", categoryId, false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.generateNextCode()).thenReturn("PROD-001");
        when(priceTypeRepository.findById(priceTypeId)).thenReturn(Optional.of(mock(PriceType.class)));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(productRepository).save(any(Product.class));
        verify(productRepository).generateNextCode();
    }

    @Test
    void should_CreateCompositeWithComponents_When_CompositeRequest() {
        UUID categoryId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        CreateProductRequest.ComponentRequest compReq = new CreateProductRequest.ComponentRequest(componentId, 2);
        CreateProductRequest request = new CreateProductRequest(
                null, "Composite Product", "Description", categoryId, true,
                null, List.of(compReq), null);

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Kit");

        Product componentProduct = Product.reconstitute(componentId, "COMP-001", "Component",
                "desc", categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Product savedProduct = Product.reconstitute(UUID.randomUUID(), "COMP-KIT-001", "Composite Product",
                "Description", categoryId, true, true, null,
                null, List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.generateNextCode()).thenReturn("COMP-KIT-001");
        when(productRepository.findById(componentId)).thenReturn(Optional.of(componentProduct));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(request);

        assertNotNull(result);
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void should_ThrowException_When_CategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                null, "Test Product", "Description", categoryId, false,
                null, null, null);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_PriceTypeNotFound() {
        UUID categoryId = UUID.randomUUID();
        UUID priceTypeId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                null, "Test Product", "Description", categoryId, false,
                null, null,
                List.of(new ProductPriceRequest(priceTypeId, new BigDecimal("10.00"), null)));

        Category category = mock(Category.class);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.generateNextCode()).thenReturn("PROD-001");
        when(priceTypeRepository.findById(priceTypeId)).thenReturn(Optional.empty());

        assertThrows(PriceTypeNotFoundException.class, () -> useCase.execute(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_DuplicateComponent() {
        UUID categoryId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        CreateProductRequest.ComponentRequest compReq = new CreateProductRequest.ComponentRequest(componentId, 2);
        CreateProductRequest request = new CreateProductRequest(
                null, "Composite", "Description", categoryId, true,
                null, List.of(compReq, compReq), null);

        Product componentProduct = Product.reconstitute(componentId, "COMP-001", "Component",
                "desc", categoryId, false, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Product savedProduct = Product.reconstitute(UUID.randomUUID(), "KIT-001", "Composite",
                "Description", categoryId, true, true, null,
                null, List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(productRepository.generateNextCode()).thenReturn("KIT-001");
        when(productRepository.findById(componentId)).thenReturn(Optional.of(componentProduct));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        assertThrows(DuplicateProductComponentException.class, () -> useCase.execute(request));
    }

    @Test
    void should_ThrowException_When_ComponentIsComposite() {
        UUID categoryId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        CreateProductRequest.ComponentRequest compReq = new CreateProductRequest.ComponentRequest(componentId, 2);
        CreateProductRequest request = new CreateProductRequest(
                null, "Composite", "Description", categoryId, true,
                null, List.of(compReq), null);

        Product compositeComponent = Product.reconstitute(componentId, "COMP-001", "Component",
                "desc", categoryId, true, true, null, null,
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Product savedProduct = Product.reconstitute(UUID.randomUUID(), "KIT-001", "Composite",
                "Description", categoryId, true, true, null,
                null, List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(productRepository.generateNextCode()).thenReturn("KIT-001");
        when(productRepository.findById(componentId)).thenReturn(Optional.of(compositeComponent));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        assertThrows(CompositeProductCannotBeComponentException.class, () -> useCase.execute(request));
    }
}
