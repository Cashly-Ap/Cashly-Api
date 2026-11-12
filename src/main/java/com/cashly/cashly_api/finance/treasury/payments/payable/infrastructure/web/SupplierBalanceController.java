package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case.CreateSupplierAdvanceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case.GetSupplierBalanceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.balance.use_case.RefundSupplierBalanceUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.SupplierBalanceMovementRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierBalanceResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierBalanceTransactionResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers/{supplierId}/balance")
public class SupplierBalanceController {

    private final GetSupplierBalanceUseCase getBalance;
    private final CreateSupplierAdvanceUseCase createAdvance;
    private final RefundSupplierBalanceUseCase createRefund;

    public SupplierBalanceController(GetSupplierBalanceUseCase getBalance,
            CreateSupplierAdvanceUseCase createAdvance,
            RefundSupplierBalanceUseCase createRefund) {
        this.getBalance = getBalance;
        this.createAdvance = createAdvance;
        this.createRefund = createRefund;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public ResponseEntity<SupplierBalanceResponse> get(
            @PathVariable UUID supplierId) {
        return ResponseEntity.ok(getBalance.execute(supplierId));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public ResponseEntity<PageResponse<SupplierBalanceTransactionResponse>> transactions(
            @PathVariable UUID supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(getBalance.transactions(supplierId, page, size));
    }

    @PostMapping("/advance")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_ADVANCE')")
    public ResponseEntity<SupplierBalanceResponse> advance(
            @PathVariable UUID supplierId,
            @Valid @RequestBody SupplierBalanceMovementRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createAdvance.execute(supplierId, request.amount(),
                        request.paymentMethodId(), request.notes(),
                        actorId(authentication), authentication.getName()));
    }

    @PostMapping("/refund")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_REFUND')")
    public ResponseEntity<SupplierBalanceResponse> refund(
            @PathVariable UUID supplierId,
            @Valid @RequestBody SupplierBalanceMovementRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createRefund.execute(supplierId, request.amount(),
                        request.paymentMethodId(), request.notes(),
                        actorId(authentication), authentication.getName()));
    }

    private UUID actorId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return principal instanceof UUID uuid ? uuid
                : UUID.fromString(String.valueOf(principal));
    }
}
