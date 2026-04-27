package com.parsun.parsun_api.products.infrastructure.web;

import com.parsun.parsun_api.products.application.dto.request.CreateProductRequest;
import com.parsun.parsun_api.products.application.dto.request.UpdateProductRequest;
import com.parsun.parsun_api.products.application.dto.response.ProductDetailResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductListResponse;
import com.parsun.parsun_api.products.application.usecases.*;
import com.parsun.parsun_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
                              GetProductUseCase getProductUseCase,
                              ListProductsUseCase listProductsUseCase,
                              UpdateProductUseCase updateProductUseCase,
                              DeleteProductUseCase deleteProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_INVENTORY')")
    public ResponseEntity<ProductDetailResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createProductUseCase.execute(request));
    }

    @GetMapping
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
}
