package com.cashly.cashly_api.catalog.categories.infrastructure.web;

import com.cashly.cashly_api.catalog.categories.application.create.request.CreateCategoryRequest;
import com.cashly.cashly_api.catalog.categories.application.update.request.UpdateCategoryRequest;
import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryDetailResponse;
import com.cashly.cashly_api.catalog.categories.application.dto.response.CategoryListResponse;
import com.cashly.cashly_api.catalog.categories.application.create.use_case.CreateCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.delete.use_case.DeleteCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.queries.use_case.GetCategoryUseCase;
import com.cashly.cashly_api.catalog.categories.application.queries.use_case.ListCategoriesUseCase;
import com.cashly.cashly_api.catalog.categories.application.update.use_case.UpdateCategoryUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase,
            GetCategoryUseCase getCategoryUseCase,
            ListCategoriesUseCase listCategoriesUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            DeleteCategoryUseCase deleteCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATALOG_CATEGORIES:CREATE')")
    public ResponseEntity<CategoryDetailResponse> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createCategoryUseCase.execute(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATALOG_CATEGORIES:READ')")
    public ResponseEntity<PageResponse<CategoryListResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        return ResponseEntity.ok(listCategoriesUseCase.execute(search, active,
                page, size, orderBy, orderDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOG_CATEGORIES:READ')")
    public ResponseEntity<CategoryDetailResponse> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(getCategoryUseCase.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOG_CATEGORIES:UPDATE')")
    public ResponseEntity<CategoryDetailResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(updateCategoryUseCase.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOG_CATEGORIES:DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCategoryUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
