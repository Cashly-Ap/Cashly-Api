package com.cashly.cashly_api.partners.customers.credit.infrastructure.web;

import com.cashly.cashly_api.partners.customers.credit.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.partners.customers.credit.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.partners.customers.credit.application.status.use_case.GetCustomerCreditStatusUseCase;
import com.cashly.cashly_api.partners.customers.credit.application.overdue.use_case.GetOverdueInvoicesUseCase;
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
    @PreAuthorize("hasAuthority('FINANCES:READ')")
    public ResponseEntity<CreditStatusResponse> getCreditStatus(@PathVariable UUID customerId) {
        CreditStatusResponse response = getCustomerCreditStatusUseCase.execute(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/credit/overdue")
    @PreAuthorize("hasAuthority('FINANCES:READ')")
    public ResponseEntity<List<OverdueInvoiceResponse>> getOverdueInvoices() {
        List<OverdueInvoiceResponse> response = getOverdueInvoicesUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
