package com.parsun.parsun_api.finances.infrastructure.web;

import com.parsun.parsun_api.finances.application.dto.request.CreatePaymentMethodRequest;
import com.parsun.parsun_api.finances.application.dto.request.UpdatePaymentMethodRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.usecases.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finances/payment-methods")
public class PaymentMethodController {

    private final CreatePaymentMethodUseCase createPaymentMethodUseCase;
    private final UpdatePaymentMethodUseCase updatePaymentMethodUseCase;
    private final ListPaymentMethodsUseCase listPaymentMethodsUseCase;
    private final GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase;
    private final TogglePaymentMethodUseCase togglePaymentMethodUseCase;

    public PaymentMethodController(CreatePaymentMethodUseCase createPaymentMethodUseCase,
                                    UpdatePaymentMethodUseCase updatePaymentMethodUseCase,
                                    ListPaymentMethodsUseCase listPaymentMethodsUseCase,
                                    GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase,
                                    TogglePaymentMethodUseCase togglePaymentMethodUseCase) {
        this.createPaymentMethodUseCase = createPaymentMethodUseCase;
        this.updatePaymentMethodUseCase = updatePaymentMethodUseCase;
        this.listPaymentMethodsUseCase = listPaymentMethodsUseCase;
        this.getPaymentMethodByIdUseCase = getPaymentMethodByIdUseCase;
        this.togglePaymentMethodUseCase = togglePaymentMethodUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentMethodResponse> create(
            @Valid @RequestBody CreatePaymentMethodRequest request) {
        PaymentMethodResponse response = createPaymentMethodUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<PaymentMethodResponse>> list(
            @RequestParam(required = false, defaultValue = "false") boolean active) {
        List<PaymentMethodResponse> paymentMethods = listPaymentMethodsUseCase.execute(active);
        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentMethodResponse> getById(@PathVariable UUID id) {
        PaymentMethodResponse response = getPaymentMethodByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentMethodResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
        PaymentMethodResponse response = updatePaymentMethodUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentMethodResponse> toggle(@PathVariable UUID id) {
        PaymentMethodResponse response = togglePaymentMethodUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}
