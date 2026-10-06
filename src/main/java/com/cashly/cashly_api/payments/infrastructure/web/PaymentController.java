package com.cashly.cashly_api.payments.infrastructure.web;

import com.cashly.cashly_api.payments.application.dto.request.ApprovePaymentRequest;
import com.cashly.cashly_api.payments.application.dto.request.CreatePaymentRequest;
import com.cashly.cashly_api.payments.application.dto.request.PayCustomerDebtRequest;
import com.cashly.cashly_api.payments.application.dto.request.PayInvoiceRequest;
import com.cashly.cashly_api.payments.application.dto.request.PayPartialDebtRequest;
import com.cashly.cashly_api.payments.application.dto.request.RejectPaymentRequest;
import com.cashly.cashly_api.payments.application.dto.request.UpdatePaymentRequest;
import com.cashly.cashly_api.payments.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResultResponse;
import com.cashly.cashly_api.payments.application.usecases.*;
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
@RequestMapping("/api/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetAllPaymentsUseCase getAllPaymentsUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final UpdatePaymentUseCase updatePaymentUseCase;
    private final DeletePaymentUseCase deletePaymentUseCase;
    private final PayInvoiceUseCase payInvoiceUseCase;
    private final PayAllCustomerDebtUseCase payAllCustomerDebtUseCase;
    private final PayPartialDebtUseCase payPartialDebtUseCase;
    private final ApprovePaymentUseCase approvePaymentUseCase;
    private final RejectPaymentUseCase rejectPaymentUseCase;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                              GetAllPaymentsUseCase getAllPaymentsUseCase,
                              GetPaymentByIdUseCase getPaymentByIdUseCase,
                              UpdatePaymentUseCase updatePaymentUseCase,
                              DeletePaymentUseCase deletePaymentUseCase,
                              PayInvoiceUseCase payInvoiceUseCase,
                              PayAllCustomerDebtUseCase payAllCustomerDebtUseCase,
                              PayPartialDebtUseCase payPartialDebtUseCase,
                              ApprovePaymentUseCase approvePaymentUseCase,
                              RejectPaymentUseCase rejectPaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.updatePaymentUseCase = updatePaymentUseCase;
        this.deletePaymentUseCase = deletePaymentUseCase;
        this.payInvoiceUseCase = payInvoiceUseCase;
        this.payAllCustomerDebtUseCase = payAllCustomerDebtUseCase;
        this.payPartialDebtUseCase = payPartialDebtUseCase;
        this.approvePaymentUseCase = approvePaymentUseCase;
        this.rejectPaymentUseCase = rejectPaymentUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        PaymentResponse response = createPaymentUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<PaymentListResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        List<PaymentListResponse> payments = getAllPaymentsUseCase.execute(dateFrom, dateTo, customerId, type, status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        PaymentResponse payment = getPaymentByIdUseCase.execute(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResponse> approve(
            @PathVariable UUID id,
            @Valid @RequestBody ApprovePaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResponse response = approvePaymentUseCase.execute(id, request, userId, userName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResponse> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectPaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResponse response = rejectPaymentUseCase.execute(id, request, userId, userName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payInvoice(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody PayInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payInvoiceUseCase.execute(invoiceId, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/customers/{customerId}/pay-all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payAllCustomerDebt(
            @PathVariable UUID customerId,
            @RequestBody @Valid PayCustomerDebtRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payAllCustomerDebtUseCase.execute(customerId, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/customers/{customerId}/pay-partial")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payPartialDebt(
            @PathVariable UUID customerId,
            @RequestBody @Valid PayPartialDebtRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payPartialDebtUseCase.execute(customerId, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePaymentRequest request) {
        PaymentResponse response = updatePaymentUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePaymentUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
