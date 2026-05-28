package com.parsun.parsun_api.payments.infrastructure.web;

import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.request.UpdatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.*;
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

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                              GetAllPaymentsUseCase getAllPaymentsUseCase,
                              GetPaymentByIdUseCase getPaymentByIdUseCase,
                              UpdatePaymentUseCase updatePaymentUseCase,
                              DeletePaymentUseCase deletePaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getAllPaymentsUseCase = getAllPaymentsUseCase;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.updatePaymentUseCase = updatePaymentUseCase;
        this.deletePaymentUseCase = deletePaymentUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCE')")
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        PaymentResponse response = createPaymentUseCase.execute(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCE')")
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCE')")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        PaymentResponse payment = getPaymentByIdUseCase.execute(id);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCE')")
    public ResponseEntity<PaymentResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdatePaymentRequest request) {
        PaymentResponse response = updatePaymentUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePaymentUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
