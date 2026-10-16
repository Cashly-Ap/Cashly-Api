package com.cashly.cashly_api.purchases.infrastructure.web;

import com.cashly.cashly_api.purchases.application.cancel.CancelPurchaseRequest;
import com.cashly.cashly_api.purchases.application.create.CreatePurchaseRequest;
import com.cashly.cashly_api.purchases.application.update.UpdatePurchaseRequest;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.purchases.application.cancel.CancelPurchaseUseCase;
import com.cashly.cashly_api.purchases.application.confirm.ConfirmPurchaseUseCase;
import com.cashly.cashly_api.purchases.application.create.CreatePurchaseUseCase;
import com.cashly.cashly_api.purchases.application.delete.DeletePurchaseUseCase;
import com.cashly.cashly_api.purchases.application.products.GetProductsForPurchaseUseCase;
import com.cashly.cashly_api.purchases.application.get.GetPurchaseByIdUseCase;
import com.cashly.cashly_api.purchases.application.list.ListPurchasesUseCase;
import com.cashly.cashly_api.purchases.application.update.UpdatePurchaseUseCase;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<PurchaseDetailResponse> create(
            @Valid @RequestBody CreatePurchaseRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        PurchaseDetailResponse response = createPurchaseUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<List<PurchaseListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir) {
        List<PurchaseListResponse> purchases = listPurchasesUseCase.execute(
                fromDate, toDate, supplierId, status, productId, categoryId, orderBy, orderDir);
        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getProductsForPurchaseUseCase.execute(search, categoryId, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<PurchaseDetailResponse> getById(@PathVariable UUID id) {
        PurchaseDetailResponse response = getPurchaseByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<PurchaseDetailResponse> update(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdatePurchaseRequest request) {
        PurchaseDetailResponse response = updatePurchaseUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<PurchaseDetailResponse> confirm(@PathVariable UUID id) {
        PurchaseDetailResponse response = confirmPurchaseUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<PurchaseDetailResponse> cancel(@PathVariable UUID id,
                                                          @Valid @RequestBody CancelPurchaseRequest request) {
        PurchaseDetailResponse response = cancelPurchaseUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_PURCHASES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePurchaseUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
