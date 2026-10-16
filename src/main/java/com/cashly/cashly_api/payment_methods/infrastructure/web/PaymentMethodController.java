package com.cashly.cashly_api.payment_methods.infrastructure.web;

import com.cashly.cashly_api.payment_methods.application.create.CreatePaymentMethodRequest;
import com.cashly.cashly_api.payment_methods.application.update.UpdatePaymentMethodRequest;
import com.cashly.cashly_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.payment_methods.application.create.CreatePaymentMethodUseCase;
import com.cashly.cashly_api.payment_methods.application.delete.DeletePaymentMethodUseCase;
import com.cashly.cashly_api.payment_methods.application.queries.GetPaymentMethodByIdUseCase;
import com.cashly.cashly_api.payment_methods.application.queries.ListPaymentMethodsUseCase;
import com.cashly.cashly_api.payment_methods.application.toggle.TogglePaymentMethodUseCase;
import com.cashly.cashly_api.payment_methods.application.update.UpdatePaymentMethodUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final CreatePaymentMethodUseCase createPaymentMethodUseCase;
    private final UpdatePaymentMethodUseCase updatePaymentMethodUseCase;
    private final ListPaymentMethodsUseCase listPaymentMethodsUseCase;
    private final GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase;
    private final TogglePaymentMethodUseCase togglePaymentMethodUseCase;
    private final DeletePaymentMethodUseCase deletePaymentMethodUseCase;

    public PaymentMethodController(CreatePaymentMethodUseCase createPaymentMethodUseCase,
                                    UpdatePaymentMethodUseCase updatePaymentMethodUseCase,
                                    ListPaymentMethodsUseCase listPaymentMethodsUseCase,
                                    GetPaymentMethodByIdUseCase getPaymentMethodByIdUseCase,
                                    TogglePaymentMethodUseCase togglePaymentMethodUseCase,
                                    DeletePaymentMethodUseCase deletePaymentMethodUseCase) {
        this.createPaymentMethodUseCase = createPaymentMethodUseCase;
        this.updatePaymentMethodUseCase = updatePaymentMethodUseCase;
        this.listPaymentMethodsUseCase = listPaymentMethodsUseCase;
        this.getPaymentMethodByIdUseCase = getPaymentMethodByIdUseCase;
        this.togglePaymentMethodUseCase = togglePaymentMethodUseCase;
        this.deletePaymentMethodUseCase = deletePaymentMethodUseCase;
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
    public ResponseEntity<PageResponse<PaymentMethodResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String orderBy,
            @RequestParam(defaultValue = "asc") String orderDir) {
        return ResponseEntity.ok(listPaymentMethodsUseCase.execute(search, active, page, size, orderBy, orderDir));
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePaymentMethodUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
