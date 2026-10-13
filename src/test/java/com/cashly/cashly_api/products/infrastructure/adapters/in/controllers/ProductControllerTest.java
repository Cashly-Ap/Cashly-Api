package com.cashly.cashly_api.products.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.products.application.dto.request.CreateProductRequest;
import com.cashly.cashly_api.products.application.dto.request.UpdateProductRequest;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.dto.response.ProductListResponse;
import com.cashly.cashly_api.products.application.dto.response.ProductStockByWarehouseResponse;
import com.cashly.cashly_api.products.application.usecases.CreateProductUseCase;
import com.cashly.cashly_api.products.application.usecases.DeleteProductUseCase;
import com.cashly.cashly_api.products.application.usecases.GetProductStockUseCase;
import com.cashly.cashly_api.products.application.usecases.GetProductUseCase;
import com.cashly.cashly_api.products.application.usecases.ListProductsUseCase;
import com.cashly.cashly_api.products.application.usecases.RemoveProductPhotoUseCase;
import com.cashly.cashly_api.products.application.usecases.UpdateProductUseCase;
import com.cashly.cashly_api.products.application.usecases.UploadProductPhotoUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Mock
    private CreateProductUseCase createProductUseCase;

    @Mock
    private GetProductUseCase getProductUseCase;

    @Mock
    private ListProductsUseCase listProductsUseCase;

    @Mock
    private UpdateProductUseCase updateProductUseCase;

    @Mock
    private DeleteProductUseCase deleteProductUseCase;

    @Mock
    private UploadProductPhotoUseCase uploadProductPhotoUseCase;

    @Mock
    private RemoveProductPhotoUseCase removeProductPhotoUseCase;

    @Mock
    private GetProductStockUseCase getProductStockUseCase;

    @InjectMocks
    private ProductController productController;

    @Test
    @DisplayName("should create product and return 201")
    void should_create_product_and_return_201() {
        String id = UUID.randomUUID().toString();
        ProductDetailResponse response = mock(ProductDetailResponse.class);
        when(response.id()).thenReturn(id);

        when(createProductUseCase.execute(any(CreateProductRequest.class))).thenReturn(response);

        var request = new CreateProductRequest(
                null, "Test Product", "Desc", UUID.randomUUID(), false,
                BigDecimal.TEN, null, null);

        ResponseEntity<ProductDetailResponse> result = productController.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(createProductUseCase).execute(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("should get all products and return 200")
    void should_get_all_products_and_return_200() {
        ProductListResponse product = mock(ProductListResponse.class);
        PageResponse<ProductListResponse> pageResponse = new PageResponse<>(
                List.of(product), 1L, 1, 10, 0, true, true, false);

        when(listProductsUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<ProductListResponse>> result = productController.getAll(
                null, null, null, null, 0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().content().size());
        verify(listProductsUseCase).execute(null, null, null, null, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("should return empty list when no products")
    void should_return_empty_list_when_no_products() {
        PageResponse<ProductListResponse> emptyPage = new PageResponse<>(
                List.of(), 0L, 0, 10, 0, true, true, true);

        when(listProductsUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

        ResponseEntity<PageResponse<ProductListResponse>> result = productController.getAll(
                null, null, null, null, 0, 10, "name", "asc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().content().isEmpty());
        verify(listProductsUseCase).execute(null, null, null, null, 0, 10, "name", "asc");
    }

    @Test
    @DisplayName("should get product by id and return 200")
    void should_get_product_by_id_and_return_200() {
        String id = UUID.randomUUID().toString();
        ProductDetailResponse response = mock(ProductDetailResponse.class);
        when(response.id()).thenReturn(id);

        when(getProductUseCase.execute(UUID.fromString(id))).thenReturn(response);

        ResponseEntity<ProductDetailResponse> result = productController.getById(UUID.fromString(id));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getProductUseCase).execute(UUID.fromString(id));
    }

    @Test
    @DisplayName("should update product and return 200")
    void should_update_product_and_return_200() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        ProductDetailResponse response = mock(ProductDetailResponse.class);
        when(response.id()).thenReturn(idStr);

        when(updateProductUseCase.execute(eq(id), any(UpdateProductRequest.class))).thenReturn(response);

        var request = new UpdateProductRequest(
                "Updated", "Desc", UUID.randomUUID(),
                BigDecimal.TEN,
                false, null, null, null);

        ResponseEntity<ProductDetailResponse> result = productController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(idStr, result.getBody().id());
        verify(updateProductUseCase).execute(eq(id), any(UpdateProductRequest.class));
    }

    @Test
    @DisplayName("should delete product and return 204")
    void should_delete_product_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteProductUseCase).execute(id);

        ResponseEntity<Void> result = productController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteProductUseCase).execute(id);
    }

    @Test
    @DisplayName("should get product stock and return 200")
    void should_get_product_stock_and_return_200() {
        UUID id = UUID.randomUUID();
        ProductStockByWarehouseResponse stock = new ProductStockByWarehouseResponse(
                UUID.randomUUID().toString(), "Main Warehouse", 100, LocalDateTime.now());

        when(getProductStockUseCase.execute(id)).thenReturn(List.of(stock));

        ResponseEntity<List<ProductStockByWarehouseResponse>> result = productController.getStock(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(getProductStockUseCase).execute(id);
    }

    @Test
    @DisplayName("should upload photo and return 200")
    void should_upload_photo_and_return_200() throws IOException {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        ProductDetailResponse response = mock(ProductDetailResponse.class);
        when(response.id()).thenReturn(idStr);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(file.getContentType()).thenReturn("image/jpeg");

        when(uploadProductPhotoUseCase.execute(eq(id), anyString(), any(), anyString()))
                .thenReturn(response);

        ResponseEntity<ProductDetailResponse> result = productController.uploadPhoto(id, file);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(idStr, result.getBody().id());
        verify(uploadProductPhotoUseCase).execute(eq(id), eq("photo.jpg"), any(), eq("image/jpeg"));
    }

    @Test
    @DisplayName("should return 500 when upload photo fails")
    void should_return_500_when_upload_photo_fails() throws IOException {
        UUID id = UUID.randomUUID();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("photo.jpg");
        when(file.getInputStream()).thenThrow(new IOException("IO error"));

        ResponseEntity<ProductDetailResponse> result = productController.uploadPhoto(id, file);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());
        verify(uploadProductPhotoUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    @DisplayName("should remove photo and return 204")
    void should_remove_photo_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(removeProductPhotoUseCase).execute(id);

        ResponseEntity<Void> result = productController.removePhoto(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(removeProductPhotoUseCase).execute(id);
    }
}
