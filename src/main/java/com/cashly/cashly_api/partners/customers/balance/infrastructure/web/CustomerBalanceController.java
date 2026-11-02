package com.cashly.cashly_api.partners.customers.balance.infrastructure.web;

import com.cashly.cashly_api.partners.customers.balance.application.advance.request.CreateAdvanceRequest;
import com.cashly.cashly_api.partners.customers.balance.application.refund.request.CreateRefundRequest;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.BalanceTransactionResponse;
import com.cashly.cashly_api.partners.customers.balance.application.dto.response.CustomerBalanceResponse;
import com.cashly.cashly_api.partners.customers.balance.application.advance.use_case.CreateCustomerAdvanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.refund.use_case.RefundCustomerBalanceUseCase;
import com.cashly.cashly_api.partners.customers.balance.application.get.use_case.GetCustomerBalanceUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers/{customerId}/balance")
public class CustomerBalanceController {

    private final GetCustomerBalanceUseCase getCustomerBalanceUseCase;
    private final CreateCustomerAdvanceUseCase createCustomerAdvanceUseCase;
    private final RefundCustomerBalanceUseCase refundCustomerBalanceUseCase;

    public CustomerBalanceController(
            GetCustomerBalanceUseCase getCustomerBalanceUseCase,
            CreateCustomerAdvanceUseCase createCustomerAdvanceUseCase,
            RefundCustomerBalanceUseCase refundCustomerBalanceUseCase) {
        this.getCustomerBalanceUseCase = getCustomerBalanceUseCase;
        this.createCustomerAdvanceUseCase = createCustomerAdvanceUseCase;
        this.refundCustomerBalanceUseCase = refundCustomerBalanceUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCE_RECEIVABLES:BALANCE')")
    public ResponseEntity<CustomerBalanceResponse> getBalance(
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(getCustomerBalanceUseCase.execute(customerId));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('FINANCE_RECEIVABLES:BALANCE')")
    public ResponseEntity<PageResponse<BalanceTransactionResponse>> getTransactions(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getCustomerBalanceUseCase
                .getTransactions(customerId, page, size));
    }

    @PostMapping("/advance")
    @PreAuthorize("hasAuthority('FINANCE_RECEIVABLES:ADVANCE')")
    public ResponseEntity<CustomerBalanceResponse> createAdvance(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAdvanceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CustomerBalanceResponse response = createCustomerAdvanceUseCase.execute(
                customerId, request.amount(), request.paymentMethodId(),
                request.notes(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refund")
    @PreAuthorize("hasAuthority('FINANCE_RECEIVABLES:REFUND')")
    public ResponseEntity<CustomerBalanceResponse> createRefund(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateRefundRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CustomerBalanceResponse response = refundCustomerBalanceUseCase.execute(
                customerId, request.amount(), request.paymentMethodId(),
                request.notes(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
