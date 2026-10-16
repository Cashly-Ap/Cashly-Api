package com.cashly.cashly_api.products.application.queries.use_case;

import com.cashly.cashly_api.categories.domain.entity.Category;
import com.cashly.cashly_api.categories.domain.repository.CategoryRepository;
import com.cashly.cashly_api.products.application.dto.response.ProductListResponse;
import com.cashly.cashly_api.products.application.mappers.ProductMapper;
import com.cashly.cashly_api.products.domain.entity.Product;
import com.cashly.cashly_api.products.domain.repository.ProductRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import com.cashly.cashly_api.products.infrastructure.integration.LocalProductCategoryGatewayAdapter;
import com.cashly.cashly_api.products.infrastructure.integration.LocalProductPriceTypeGatewayAdapter;
import com.cashly.cashly_api.categories.application.queries.services.RepositoryProductCategoryQuery;
import com.cashly.cashly_api.price_types.application.queries.services.RepositoryProductPriceTypeQuery;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListProductsUseCase Tests")
class ListProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    private ListProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListProductsUseCase(productRepository, new LocalProductCategoryGatewayAdapter(new RepositoryProductCategoryQuery(categoryRepository)), productMapper);
    }

    @Test
    void should_ReturnPaginatedList_When_ProductsExist() {
        UUID categoryId = UUID.randomUUID();
        Product product = Product.reconstitute(UUID.randomUUID(), "PROD-001", "Test Product", "Description",
                categoryId, false, true, null,
                new BigDecimal("5.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        ProductListResponse listResponse = new ProductListResponse(
                product.getId().toString(), product.getCode(), product.getName(),
                categoryId.toString(), "Electronics", false, true,
                new BigDecimal("10.00"), List.of(), true);

        PageResponse<Product> page = new PageResponse<>(
                List.of(product), 1L, 1, 10, 0, true, true, false);

        when(productRepository.findAllWithFilters(null, null, null, null, 0, 10, "name", "asc"))
                .thenReturn(page);
        when(productRepository.findIdsWithReferences(any())).thenReturn(Set.of());
        when(productMapper.toListResponse(any(Product.class), anyString(), anyBoolean())).thenReturn(listResponse);

        PageResponse<ProductListResponse> result = useCase.execute(null, null, null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1L, result.totalElements());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void should_ReturnEmptyPage_When_NoProducts() {
        PageResponse<Product> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(productRepository.findAllWithFilters(null, null, null, null, 0, 10, "name", "asc"))
                .thenReturn(emptyPage);

        PageResponse<ProductListResponse> result = useCase.execute(null, null, null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0L, result.totalElements());
        assertTrue(result.empty());
        verify(categoryRepository, never()).findById(any());
    }
}
