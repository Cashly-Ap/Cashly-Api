package com.cashly.cashly_api.customers.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.application.usecases.credit.GetCustomerCreditStatusUseCase;
import com.cashly.cashly_api.customers.application.usecases.credit.GetOverdueInvoicesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerCreditController {

    private final GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase;
    private final GetOverdueInvoicesUseCase getOverdueInvoicesUseCase;

    public CustomerCreditController(GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase,
                                     GetOverdueInvoicesUseCase getOverdueInvoicesUseCase) {
        this.getCustomerCreditStatusUseCase = getCustomerCreditStatusUseCase;
        this.getOverdueInvoicesUseCase = getOverdueInvoicesUseCase;
    }

    @GetMapping("/{customerId}/credit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CreditStatusResponse> getCreditStatus(@PathVariable UUID customerId) {
        CreditStatusResponse response = getCustomerCreditStatusUseCase.execute(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/credit/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<OverdueInvoiceResponse>> getOverdueInvoices() {
        List<OverdueInvoiceResponse> response = getOverdueInvoicesUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
