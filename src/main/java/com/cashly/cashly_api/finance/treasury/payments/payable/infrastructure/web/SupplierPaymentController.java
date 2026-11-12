package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payments.payable.application.approve.use_case.ApproveSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.create.use_case.RegisterSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.delete.use_case.DeleteSupplierPaymentUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.CreateSupplierPaymentRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.PayPurchaseRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.PaySupplierDebtRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.request.SupplierPaymentDecisionRequest;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.dto.response.SupplierPaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.list.use_case.ListSupplierPaymentsUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.pay_purchase.use_case.PayPurchaseUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.pay_supplier_debt.use_case.PaySupplierDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payments.payable.application.reject.use_case.RejectSupplierPaymentUseCase;
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
@RequestMapping("/api/supplier-payments")
public class SupplierPaymentController {

    private final RegisterSupplierPaymentUseCase register;
    private final PayPurchaseUseCase payPurchase;
    private final PaySupplierDebtUseCase payDebt;
    private final ApproveSupplierPaymentUseCase approve;
    private final RejectSupplierPaymentUseCase reject;
    private final DeleteSupplierPaymentUseCase delete;
    private final ListSupplierPaymentsUseCase list;

    public SupplierPaymentController(RegisterSupplierPaymentUseCase register,
            PayPurchaseUseCase payPurchase, PaySupplierDebtUseCase payDebt,
            ApproveSupplierPaymentUseCase approve,
            RejectSupplierPaymentUseCase reject,
            DeleteSupplierPaymentUseCase delete,
            ListSupplierPaymentsUseCase list) {
        this.register = register;
        this.payPurchase = payPurchase;
        this.payDebt = payDebt;
        this.approve = approve;
        this.reject = reject;
        this.delete = delete;
        this.list = list;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_PAY_WITH_VALIDATION')")
    public ResponseEntity<SupplierPaymentResponse> create(
            @Valid @RequestBody CreateSupplierPaymentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(register.execute(
                request, actorId(authentication), authentication.getName()));
    }

    @PostMapping("/purchases/{purchaseId}/pay")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_PAY_DIRECT')")
    public ResponseEntity<SupplierPaymentResponse> payPurchase(
            @PathVariable UUID purchaseId,
            @Valid @RequestBody PayPurchaseRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(payPurchase.execute(purchaseId, request,
                actorId(authentication), authentication.getName()));
    }

    @PostMapping("/suppliers/{supplierId}/pay-all")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_PAY_ALL')")
    public ResponseEntity<SupplierPaymentResponse> payAll(
            @PathVariable UUID supplierId,
            @Valid @RequestBody PaySupplierDebtRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(payDebt.execute(supplierId, request,
                actorId(authentication), authentication.getName()));
    }

    @PostMapping("/suppliers/{supplierId}/pay-partial")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_PARTIAL_PAYMENT')")
    public ResponseEntity<SupplierPaymentResponse> payPartial(
            @PathVariable UUID supplierId,
            @Valid @RequestBody PaySupplierDebtRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(payDebt.execute(supplierId, request,
                actorId(authentication), authentication.getName()));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_APPROVE_PAYMENT')")
    public ResponseEntity<SupplierPaymentResponse> approve(@PathVariable UUID id,
            @Valid @RequestBody SupplierPaymentDecisionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(approve.execute(id, request.reason(),
                actorId(authentication), authentication.getName()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_REJECT_PAYMENT')")
    public ResponseEntity<SupplierPaymentResponse> reject(@PathVariable UUID id,
            @Valid @RequestBody SupplierPaymentDecisionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(reject.execute(id, request.reason(),
                actorId(authentication), authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public ResponseEntity<List<SupplierPaymentResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String status) {
        return ResponseEntity
                .ok(list.execute(dateFrom, dateTo, supplierId, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        delete.execute(id);
        return ResponseEntity.noContent().build();
    }

    private UUID actorId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return principal instanceof UUID uuid
                ? uuid
                : UUID.fromString(String.valueOf(principal));
    }
}
