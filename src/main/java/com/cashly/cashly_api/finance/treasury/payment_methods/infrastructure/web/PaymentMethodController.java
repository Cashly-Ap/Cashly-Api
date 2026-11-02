package com.cashly.cashly_api.finance.treasury.payment_methods.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payment_methods.application.create.request.CreatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.update.request.UpdatePaymentMethodRequest;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.create.use_case.CreatePaymentMethodUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.delete.use_case.DeletePaymentMethodUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.queries.use_case.GetPaymentMethodByIdUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.queries.use_case.ListPaymentMethodsUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.toggle.use_case.TogglePaymentMethodUseCase;
import com.cashly.cashly_api.finance.treasury.payment_methods.application.update.use_case.UpdatePaymentMethodUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
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

    public PaymentMethodController(
            CreatePaymentMethodUseCase createPaymentMethodUseCase,
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
    @PreAuthorize("hasAuthority('FINANCE_PAYMENT_METHODS:CREATE')")
    public ResponseEntity<PaymentMethodResponse> create(
            @Valid @RequestBody CreatePaymentMethodRequest request) {
        PaymentMethodResponse response = createPaymentMethodUseCase
                .execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_PAYMENT_METHODS:READ')")
    public ResponseEntity<PageResponse<PaymentMethodResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String orderBy,
             @RequestParam(defaultValue = "asc") String orderDir,
             @RequestParam(required = false) DocumentAction action) {
        return ResponseEntity.ok(listPaymentMethodsUseCase.execute(search,
                 active, page, size, orderBy, orderDir, action));
    }

    public ResponseEntity<PageResponse<PaymentMethodResponse>> list(String search, Boolean active,
            int page, int size, String orderBy, String orderDir) {
        return list(search, active, page, size, orderBy, orderDir, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_PAYMENT_METHODS:READ')")
    public ResponseEntity<PaymentMethodResponse> getById(
            @PathVariable UUID id) {
        PaymentMethodResponse response = getPaymentMethodByIdUseCase
                .execute(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_PAYMENT_METHODS:UPDATE')")
    public ResponseEntity<PaymentMethodResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdatePaymentMethodRequest request) {
        PaymentMethodResponse response = updatePaymentMethodUseCase.execute(id,
                request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('FINANCE_PAYMENT_METHODS:UPDATE')")
    public ResponseEntity<PaymentMethodResponse> toggle(@PathVariable UUID id) {
        PaymentMethodResponse response = togglePaymentMethodUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCE_PAYMENT_METHODS:DELETE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deletePaymentMethodUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
