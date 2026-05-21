package com.parsun.parsun_api.finances.infrastructure.web;

import com.parsun.parsun_api.finances.application.dto.request.ApplyPaymentRequest;
import com.parsun.parsun_api.finances.application.dto.request.RecordAdvanceRequest;
import com.parsun.parsun_api.finances.application.dto.request.RegisterPaymentRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;
import com.parsun.parsun_api.finances.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.finances.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finances/payments")
public class PaymentController {

    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final ApplyPaymentToInvoicesUseCase applyPaymentToInvoicesUseCase;
    private final RecordAdvanceUseCase recordAdvanceUseCase;
    private final ApplyAdvanceToInvoiceUseCase applyAdvanceToInvoiceUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final GetPaymentsUseCase getPaymentsUseCase;
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;

    public PaymentController(RegisterPaymentUseCase registerPaymentUseCase,
                             ApplyPaymentToInvoicesUseCase applyPaymentToInvoicesUseCase,
                             RecordAdvanceUseCase recordAdvanceUseCase,
                             ApplyAdvanceToInvoiceUseCase applyAdvanceToInvoiceUseCase,
                             CancelPaymentUseCase cancelPaymentUseCase,
                             GetPaymentsUseCase getPaymentsUseCase,
                             GetPaymentByIdUseCase getPaymentByIdUseCase) {
        this.registerPaymentUseCase = registerPaymentUseCase;
        this.applyPaymentToInvoicesUseCase = applyPaymentToInvoicesUseCase;
        this.recordAdvanceUseCase = recordAdvanceUseCase;
        this.applyAdvanceToInvoiceUseCase = applyAdvanceToInvoiceUseCase;
        this.cancelPaymentUseCase = cancelPaymentUseCase;
        this.getPaymentsUseCase = getPaymentsUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentDetailResponse> register(
            @Valid @RequestBody RegisterPaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        PaymentDetailResponse response = registerPaymentUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<PaymentListResponse>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        List<PaymentListResponse> payments = getPaymentsUseCase.execute(dateFrom, dateTo, customerId, type, status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentDetailResponse> getById(@PathVariable UUID id) {
        PaymentDetailResponse response = getPaymentByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentDetailResponse> apply(@PathVariable UUID id,
                                                       @Valid @RequestBody ApplyPaymentRequest request) {
        PaymentDetailResponse response = applyPaymentToInvoicesUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentDetailResponse> cancel(@PathVariable UUID id) {
        PaymentDetailResponse response = cancelPaymentUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/advances")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentDetailResponse> recordAdvance(
            @Valid @RequestBody RecordAdvanceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        PaymentDetailResponse response = recordAdvanceUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/advances/{advanceId}/apply")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentDetailResponse> applyAdvance(@PathVariable UUID advanceId,
                                                              @RequestParam UUID invoiceId,
                                                              @RequestParam BigDecimal amount) {
        PaymentDetailResponse response = applyAdvanceToInvoiceUseCase.execute(advanceId, invoiceId, amount);
        return ResponseEntity.ok(response);
    }
}
