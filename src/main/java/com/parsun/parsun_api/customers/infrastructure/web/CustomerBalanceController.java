package com.parsun.parsun_api.customers.infrastructure.web;

import com.parsun.parsun_api.customers.application.dto.request.CreateAdvanceRequest;
import com.parsun.parsun_api.customers.application.dto.response.BalanceTransactionResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.application.usecases.AddToCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.application.usecases.GetCustomerBalanceUseCase;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import com.parsun.parsun_api.shared.dto.PageResponse;
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
    private final AddToCustomerBalanceUseCase addToCustomerBalanceUseCase;

    public CustomerBalanceController(GetCustomerBalanceUseCase getCustomerBalanceUseCase,
                                      AddToCustomerBalanceUseCase addToCustomerBalanceUseCase) {
        this.getCustomerBalanceUseCase = getCustomerBalanceUseCase;
        this.addToCustomerBalanceUseCase = addToCustomerBalanceUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CustomerBalanceResponse> getBalance(@PathVariable UUID customerId) {
        return ResponseEntity.ok(getCustomerBalanceUseCase.execute(customerId));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<BalanceTransactionResponse>> getTransactions(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(getCustomerBalanceUseCase.getTransactions(customerId, page, size));
    }

    @PostMapping("/advance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CustomerBalanceResponse> createAdvance(
            @PathVariable UUID customerId,
            @Valid @RequestBody CreateAdvanceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        CustomerBalanceResponse response = addToCustomerBalanceUseCase.execute(
                customerId,
                BalanceType.ADVANCED,
                request.amount(),
                BalanceType.ADVANCED,
                null,
                request.notes(),
                userId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
