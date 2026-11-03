package com.cashly.cashly_api.procurement.purchases.infrastructure.web;

import com.cashly.cashly_api.procurement.purchases.application.cancel.request.CancelPurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.create.request.CreatePurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.update.request.UpdatePurchaseRequest;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.procurement.purchases.application.cancel.use_case.CancelPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.confirm.use_case.ConfirmPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.create.use_case.CreatePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.delete.use_case.DeletePurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.products.use_case.GetProductsForPurchaseUseCase;
import com.cashly.cashly_api.procurement.purchases.application.get.use_case.GetPurchaseByIdUseCase;
import com.cashly.cashly_api.procurement.purchases.application.list.use_case.ListPurchasesUseCase;
import com.cashly.cashly_api.procurement.purchases.application.update.use_case.UpdatePurchaseUseCase;
import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final CreatePurchaseUseCase createPurchaseUseCase;
    private final GetPurchaseByIdUseCase getPurchaseByIdUseCase;
    private final ListPurchasesUseCase listPurchasesUseCase;
    private final UpdatePurchaseUseCase updatePurchaseUseCase;
    private final ConfirmPurchaseUseCase confirmPurchaseUseCase;
    private final CancelPurchaseUseCase cancelPurchaseUseCase;
    private final DeletePurchaseUseCase deletePurchaseUseCase;

    private final GetProductsForPurchaseUseCase getProductsForPurchaseUseCase;

    public PurchaseController(CreatePurchaseUseCase createPurchaseUseCase,
            GetPurchaseByIdUseCase getPurchaseByIdUseCase,
            ListPurchasesUseCase listPurchasesUseCase,
            UpdatePurchaseUseCase updatePurchaseUseCase,
            ConfirmPurchaseUseCase confirmPurchaseUseCase,
            CancelPurchaseUseCase cancelPurchaseUseCase,
            DeletePurchaseUseCase deletePurchaseUseCase,
            GetProductsForPurchaseUseCase getProductsForPurchaseUseCase) {
        this.createPurchaseUseCase = createPurchaseUseCase;
        this.getPurchaseByIdUseCase = getPurchaseByIdUseCase;
        this.listPurchasesUseCase = listPurchasesUseCase;
        this.updatePurchaseUseCase = updatePurchaseUseCase;
        this.confirmPurchaseUseCase = confirmPurchaseUseCase;
        this.cancelPurchaseUseCase = cancelPurchaseUseCase;
        this.deletePurchaseUseCase = deletePurchaseUseCase;
        this.getProductsForPurchaseUseCase = getProductsForPurchaseUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASES_CREATE') and hasAuthority('INVENTORY_WAREHOUSES_VIEW_STOCK')")
    public ResponseEntity<PurchaseDetailResponse> create(
            @Valid @RequestBody CreatePurchaseRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        PurchaseDetailResponse response = createPurchaseUseCase.execute(request,
                userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASES_VIEW_LIST')")
    public ResponseEntity<List<PurchaseListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir,
            @RequestParam(required = false) DocumentAction action) {
        List<PurchaseListResponse> purchases = action == null
                ? listPurchasesUseCase.execute(fromDate, toDate, supplierId,
                        status, productId, categoryId, orderBy, orderDir)
                : listPurchasesUseCase.execute(fromDate, toDate, supplierId,
                        status, productId, categoryId, orderBy, orderDir,
                        action);
        return ResponseEntity.ok(purchases.stream()
                .filter(purchase -> matchesSearch(search,
                        purchase.purchaseNumber(), purchase.supplierName()))
                .toList());
    }

    private boolean matchesSearch(String search, String... values) {
        if (search == null || search.isBlank())
            return true;
        String term = search.trim().toLowerCase();
        return java.util.Arrays.stream(values)
                .filter(java.util.Objects::nonNull)
                .anyMatch(value -> value.toLowerCase().contains(term));
    }

    public ResponseEntity<List<PurchaseListResponse>> getAll(LocalDate fromDate,
            LocalDate toDate, UUID supplierId, String status, UUID productId,
            UUID categoryId, String orderBy, String orderDir) {
        return getAll(fromDate, toDate, supplierId, status, productId,
                categoryId, null, orderBy, orderDir, null);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('PURCHASES_VIEW_LIST') and hasAuthority('INVENTORY_WAREHOUSES_VIEW_STOCK')")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getProductsForPurchaseUseCase.execute(search,
                categoryId, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASES_VIEW_DETAIL') and hasAuthority('INVENTORY_WAREHOUSES_VIEW_STOCK')")
    public ResponseEntity<PurchaseDetailResponse> getById(
            @PathVariable UUID id) {
        PurchaseDetailResponse response = getPurchaseByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASES_EDIT') and hasAuthority('INVENTORY_WAREHOUSES_VIEW_STOCK')")
    public ResponseEntity<PurchaseDetailResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseRequest request) {
        PurchaseDetailResponse response = updatePurchaseUseCase.execute(id,
                request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('PURCHASES_CONFIRM') and hasAuthority('INVENTORY_WAREHOUSES_RECORD_ENTRY')")
    public ResponseEntity<PurchaseDetailResponse> confirm(
            @PathVariable UUID id) {
        PurchaseDetailResponse response = confirmPurchaseUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PURCHASES_CANCEL') and hasAuthority('INVENTORY_WAREHOUSES_RECORD_EXIT')")
    public ResponseEntity<PurchaseDetailResponse> cancel(@PathVariable UUID id,
            @Valid @RequestBody CancelPurchaseRequest request) {
        PurchaseDetailResponse response = cancelPurchaseUseCase.execute(id,
                request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASES_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePurchaseUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
