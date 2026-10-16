package com.cashly.cashly_api.products.infrastructure.web;

import com.cashly.cashly_api.products.application.create.CreateProductRequest;
import com.cashly.cashly_api.products.application.update.UpdateProductRequest;
import com.cashly.cashly_api.products.application.dto.response.ProductDetailResponse;
import com.cashly.cashly_api.products.application.dto.response.ProductListResponse;
import com.cashly.cashly_api.products.application.stock.ProductStockByWarehouseResponse;
import com.cashly.cashly_api.products.application.create.CreateProductUseCase;
import com.cashly.cashly_api.products.application.delete.DeleteProductUseCase;
import com.cashly.cashly_api.products.application.stock.GetProductStockUseCase;
import com.cashly.cashly_api.products.application.queries.GetProductUseCase;
import com.cashly.cashly_api.products.application.transactions.GetProductsForTransactionUseCase;
import com.cashly.cashly_api.products.application.queries.ListProductsUseCase;
import com.cashly.cashly_api.products.application.photos.RemoveProductPhotoUseCase;
import com.cashly.cashly_api.products.application.update.UpdateProductUseCase;
import com.cashly.cashly_api.products.application.photos.UploadProductPhotoUseCase;
import com.cashly.cashly_api.products.application.transactions.ProductWithStockResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UploadProductPhotoUseCase uploadProductPhotoUseCase;
    private final RemoveProductPhotoUseCase removeProductPhotoUseCase;
    private final GetProductStockUseCase getProductStockUseCase;
    private final GetProductsForTransactionUseCase getProductsForTransactionUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
                              GetProductUseCase getProductUseCase,
                              ListProductsUseCase listProductsUseCase,
                              UpdateProductUseCase updateProductUseCase,
                              DeleteProductUseCase deleteProductUseCase,
                              UploadProductPhotoUseCase uploadProductPhotoUseCase,
                              RemoveProductPhotoUseCase removeProductPhotoUseCase,
                              GetProductStockUseCase getProductStockUseCase,
                              GetProductsForTransactionUseCase getProductsForTransactionUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.uploadProductPhotoUseCase = uploadProductPhotoUseCase;
        this.removeProductPhotoUseCase = removeProductPhotoUseCase;
        this.getProductStockUseCase = getProductStockUseCase;
        this.getProductsForTransactionUseCase = getProductsForTransactionUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<ProductDetailResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createProductUseCase.execute(request));
    }

    @GetMapping(params = "!customerId")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<PageResponse<ProductListResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean composite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        return ResponseEntity.ok(
            listProductsUseCase.execute(search, categoryId, active, composite, page, size, orderBy, orderDir));
    }

    @GetMapping(params = "customerId")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
    public ResponseEntity<PageResponse<ProductWithStockResponse>> getForTransaction(
            @RequestParam UUID customerId,
            @RequestParam(required = false) List<UUID> customerIds,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean composite,
            @RequestParam(required = false, defaultValue = "true") Boolean withStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ProductWithStockResponse> response =
                getProductsForTransactionUseCase.execute(
                        customerIds == null || customerIds.isEmpty() ? List.of(customerId) : customerIds,
                        search, categoryId, composite, withStock, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<ProductDetailResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getProductUseCase.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<ProductDetailResponse> update(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(updateProductUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<List<ProductStockByWarehouseResponse>> getStock(@PathVariable UUID id) {
        return ResponseEntity.ok(getProductStockUseCase.execute(id));
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<ProductDetailResponse> uploadPhoto(@PathVariable UUID id,
                                                              @RequestParam("file") MultipartFile file) {
        try {
            ProductDetailResponse response = uploadProductPhotoUseCase.execute(
                    id, file.getOriginalFilename(), file.getInputStream(), file.getContentType());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}/photo")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<Void> removePhoto(@PathVariable UUID id) {
        removeProductPhotoUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
