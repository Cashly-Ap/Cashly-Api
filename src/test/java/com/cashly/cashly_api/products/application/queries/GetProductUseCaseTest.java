package com.cashly.cashly_api.products.application.queries;

import com.cashly.cashly_api.categories.domain.Category;
import com.cashly.cashly_api.categories.domain.CategoryRepository;
import com.cashly.cashly_api.price_types.domain.PriceTypeRepository;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.Product;
import com.cashly.cashly_api.products.domain.ProductRepository;
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
@DisplayName("GetProductUseCase Tests")
class GetProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PriceTypeRepository priceTypeRepository;

    @Mock
    private ProductMapper productMapper;

    private GetProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductUseCase(productRepository, new LocalProductCategoryGatewayAdapter(new RepositoryProductCategoryQuery(categoryRepository)), new LocalProductPriceTypeGatewayAdapter(new RepositoryProductPriceTypeQuery(priceTypeRepository)), productMapper);
    }

    @Test
    void should_ReturnProduct_When_ProductExists() {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Product product = Product.reconstitute(id, "PROD-001", "Test Product", "Description",
                categoryId, false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        ProductDetailResponse expectedResponse = mock(ProductDetailResponse.class);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toDetailResponse(any(), anyString(), any(), any())).thenReturn(expectedResponse);

        ProductDetailResponse result = useCase.execute(id);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void should_ThrowException_When_ProductNotFound() {
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(id));
        verify(categoryRepository, never()).findById(any());
    }
}
