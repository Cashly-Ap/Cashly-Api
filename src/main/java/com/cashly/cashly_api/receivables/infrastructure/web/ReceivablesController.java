package com.cashly.cashly_api.receivables.infrastructure.web;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.cashly.cashly_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.cashly.cashly_api.receivables.application.customer_debt.GetCustomerDebtUseCase;
import com.cashly.cashly_api.receivables.application.customer_invoices.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.receivables.application.payment_history.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.receivables.application.summary.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.receivables.application.customer_debt.SearchCustomerDebtUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
public class ReceivablesController {

    private final GetReceivablesSummaryUseCase getReceivablesSummaryUseCase;
    private final SearchCustomerDebtUseCase searchCustomerDebtUseCase;
    private final GetCustomerDebtUseCase getCustomerDebtUseCase;
    private final GetCustomerInvoicesUseCase getCustomerInvoicesUseCase;
    private final GetPaymentHistoryUseCase getPaymentHistoryUseCase;

    public ReceivablesController(GetReceivablesSummaryUseCase getReceivablesSummaryUseCase,
                                  SearchCustomerDebtUseCase searchCustomerDebtUseCase,
                                  GetCustomerDebtUseCase getCustomerDebtUseCase,
                                  GetCustomerInvoicesUseCase getCustomerInvoicesUseCase,
                                  GetPaymentHistoryUseCase getPaymentHistoryUseCase) {
        this.getReceivablesSummaryUseCase = getReceivablesSummaryUseCase;
        this.searchCustomerDebtUseCase = searchCustomerDebtUseCase;
        this.getCustomerDebtUseCase = getCustomerDebtUseCase;
        this.getCustomerInvoicesUseCase = getCustomerInvoicesUseCase;
        this.getPaymentHistoryUseCase = getPaymentHistoryUseCase;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<ReceivablesSummaryResponse> getSummary() {
        ReceivablesSummaryResponse response = getReceivablesSummaryUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getCustomersWithDebt(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        PageResponse<?> response = searchCustomerDebtUseCase.execute(search, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CustomerDebtSummaryResponse> getCustomerDebt(@PathVariable UUID id) {
        CustomerDebtSummaryResponse response = getCustomerDebtUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getCustomerInvoices(
            @PathVariable UUID id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = getCustomerInvoicesUseCase.execute(id, search, status, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getPaymentHistory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = getPaymentHistoryUseCase.execute(search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}/payment-history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getCustomerPaymentHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID invoiceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        PageResponse<?> response = getPaymentHistoryUseCase.execute(id, invoiceId, page, size);
        return ResponseEntity.ok(response);
    }
}
