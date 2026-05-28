package com.parsun.parsun_api.receivables.infrastructure.web;

import com.parsun.parsun_api.receivables.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.receivables.application.dto.response.OverdueInvoiceResponse;
import com.parsun.parsun_api.receivables.application.usecases.GetCustomerCreditStatusUseCase;
import com.parsun.parsun_api.receivables.application.usecases.GetOverdueInvoicesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/credit")
public class CreditController {

    private final GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase;
    private final GetOverdueInvoicesUseCase getOverdueInvoicesUseCase;

    public CreditController(GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase,
                            GetOverdueInvoicesUseCase getOverdueInvoicesUseCase) {
        this.getCustomerCreditStatusUseCase = getCustomerCreditStatusUseCase;
        this.getOverdueInvoicesUseCase = getOverdueInvoicesUseCase;
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CreditStatusResponse> getCreditStatus(@PathVariable UUID customerId) {
        CreditStatusResponse response = getCustomerCreditStatusUseCase.execute(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<List<OverdueInvoiceResponse>> getOverdueInvoices() {
        List<OverdueInvoiceResponse> response = getOverdueInvoicesUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
