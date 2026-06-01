package com.parsun.parsun_api.receivables.infrastructure.web;

import com.parsun.parsun_api.receivables.application.dto.request.PayCustomerDebtRequest;
import com.parsun.parsun_api.receivables.application.dto.request.PayInvoiceRequest;
import com.parsun.parsun_api.receivables.application.dto.request.PayPartialDebtRequest;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.parsun.parsun_api.receivables.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.parsun.parsun_api.receivables.application.usecases.*;
import com.parsun.parsun_api.shared.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
public class ReceivablesController {

    private final GetReceivablesSummaryUseCase getReceivablesSummaryUseCase;
    private final SearchCustomerDebtUseCase searchCustomerDebtUseCase;
    private final GetCustomerDebtUseCase getCustomerDebtUseCase;
    private final GetCustomerInvoicesUseCase getCustomerInvoicesUseCase;
    private final PayInvoiceUseCase payInvoiceUseCase;
    private final PayAllCustomerDebtUseCase payAllCustomerDebtUseCase;
    private final PayPartialDebtUseCase payPartialDebtUseCase;
    private final GetPaymentHistoryUseCase getPaymentHistoryUseCase;

    public ReceivablesController(GetReceivablesSummaryUseCase getReceivablesSummaryUseCase,
                                  SearchCustomerDebtUseCase searchCustomerDebtUseCase,
                                  GetCustomerDebtUseCase getCustomerDebtUseCase,
                                  GetCustomerInvoicesUseCase getCustomerInvoicesUseCase,
                                  PayInvoiceUseCase payInvoiceUseCase,
                                  PayAllCustomerDebtUseCase payAllCustomerDebtUseCase,
                                  PayPartialDebtUseCase payPartialDebtUseCase,
                                  GetPaymentHistoryUseCase getPaymentHistoryUseCase) {
        this.getReceivablesSummaryUseCase = getReceivablesSummaryUseCase;
        this.searchCustomerDebtUseCase = searchCustomerDebtUseCase;
        this.getCustomerDebtUseCase = getCustomerDebtUseCase;
        this.getCustomerInvoicesUseCase = getCustomerInvoicesUseCase;
        this.payInvoiceUseCase = payInvoiceUseCase;
        this.payAllCustomerDebtUseCase = payAllCustomerDebtUseCase;
        this.payPartialDebtUseCase = payPartialDebtUseCase;
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
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = searchCustomerDebtUseCase.execute(search, page, size);
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

    @PostMapping("/invoices/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody PayInvoiceRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payInvoiceUseCase.execute(id, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/customers/{id}/pay-all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payAllCustomerDebt(
            @PathVariable UUID id,
            @RequestBody @Valid PayCustomerDebtRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payAllCustomerDebtUseCase.execute(id, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/customers/{id}/pay-partial")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payPartialDebt(
            @PathVariable UUID id,
            @RequestBody @Valid PayPartialDebtRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payPartialDebtUseCase.execute(id, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
