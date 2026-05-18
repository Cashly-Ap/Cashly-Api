package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.products.application.dto.response.ProductListResponse;
import com.parsun.parsun_api.products.application.mappers.ProductMapper;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
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
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private ListProductsUseCase useCase;

    @Test
    void should_ReturnPaginatedList_When_ProductsExist() {
        UUID categoryId = UUID.randomUUID();
        Product product = Product.reconstitute(UUID.randomUUID(), "PROD-001", "Test Product", "Description",
                categoryId, false, true, null,
                new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                List.of(), List.of(), LocalDateTime.now(), LocalDateTime.now());

        Category category = mock(Category.class);
        when(category.getName()).thenReturn("Electronics");

        ProductListResponse listResponse = new ProductListResponse(
                product.getId().toString(), product.getCode(), product.getName(),
                categoryId.toString(), "Electronics", false, true,
                new BigDecimal("10.00"), List.of());

        PageResponse<Product> page = new PageResponse<>(
                List.of(product), 1L, 1, 10, 0, true, true, false);

        when(productRepository.findAllWithFilters(null, null, null, null, 0, 10, "name", "asc"))
                .thenReturn(page);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productMapper.toListResponse(any(Product.class), eq("Electronics"))).thenReturn(listResponse);

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
