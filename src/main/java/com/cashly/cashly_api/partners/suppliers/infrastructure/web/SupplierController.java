package com.cashly.cashly_api.partners.suppliers.infrastructure.web;

import com.cashly.cashly_api.partners.suppliers.application.register.request.CreateSupplierRequest;
import com.cashly.cashly_api.partners.suppliers.application.update.request.UpdateSupplierRequest;
import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierCreateResponse;
import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierDetailResponse;
import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierListResponse;
import com.cashly.cashly_api.partners.suppliers.application.delete.use_case.DeleteSupplierUseCase;
import com.cashly.cashly_api.partners.suppliers.application.queries.use_case.GetSupplierUseCase;
import com.cashly.cashly_api.partners.suppliers.application.queries.use_case.ListSuppliersUseCase;
import com.cashly.cashly_api.partners.suppliers.application.register.use_case.RegisterSupplierUseCase;
import com.cashly.cashly_api.partners.suppliers.application.update.use_case.UpdateSupplierUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final RegisterSupplierUseCase registerSupplierUseCase;
    private final GetSupplierUseCase getSupplierUseCase;
    private final ListSuppliersUseCase listSuppliersUseCase;
    private final UpdateSupplierUseCase updateSupplierUseCase;
    private final DeleteSupplierUseCase deleteSupplierUseCase;

    public SupplierController(RegisterSupplierUseCase registerSupplierUseCase,
            GetSupplierUseCase getSupplierUseCase,
            ListSuppliersUseCase listSuppliersUseCase,
            UpdateSupplierUseCase updateSupplierUseCase,
            DeleteSupplierUseCase deleteSupplierUseCase) {
        this.registerSupplierUseCase = registerSupplierUseCase;
        this.getSupplierUseCase = getSupplierUseCase;
        this.listSuppliersUseCase = listSuppliersUseCase;
        this.updateSupplierUseCase = updateSupplierUseCase;
        this.deleteSupplierUseCase = deleteSupplierUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTACTS_SUPPLIERS_CREATE')")
    public ResponseEntity<SupplierCreateResponse> create(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierCreateResponse response = registerSupplierUseCase
                .execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTACTS_SUPPLIERS_VIEW_LIST')")
    public ResponseEntity<PageResponse<SupplierListResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "legalName") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir,
            @RequestParam(required = false) DocumentAction action) {
        PageResponse<SupplierListResponse> suppliers = listSuppliersUseCase
                .execute(search, type, active, state, page, size, orderBy,
                        orderDir, action);
        return ResponseEntity.ok(suppliers);
    }

    public ResponseEntity<PageResponse<SupplierListResponse>> getAll(String search, String type,
            Boolean active, String state, int page, int size, String orderBy, String orderDir) {
        return getAll(search, type, active, state, page, size, orderBy, orderDir, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACTS_SUPPLIERS_VIEW_DETAIL')")
    public ResponseEntity<SupplierDetailResponse> getById(
            @PathVariable String id) {
        SupplierDetailResponse supplier = getSupplierUseCase.execute(id);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACTS_SUPPLIERS_EDIT')")
    public ResponseEntity<SupplierDetailResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierDetailResponse supplier = updateSupplierUseCase.execute(id,
                request);
        return ResponseEntity.ok(supplier);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACTS_SUPPLIERS_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteSupplierUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
