package com.cashly.cashly_api.finance.treasury.payables.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.GetSupplierDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.SearchSupplierDebtUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.SupplierDebtAction;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablePaymentHistoryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablesSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtListResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierPurchaseDebtResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.payment_history.use_case.GetPayablePaymentHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.summary.use_case.GetPayablesSummaryUseCase;
import com.cashly.cashly_api.finance.treasury.payables.application.supplier_purchases.use_case.GetSupplierPurchasesUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payables")
public class PayablesController {

    private final GetPayablesSummaryUseCase summary;
    private final SearchSupplierDebtUseCase search;
    private final GetSupplierDebtUseCase detail;
    private final GetSupplierPurchasesUseCase purchases;
    private final GetPayablePaymentHistoryUseCase paymentHistory;

    public PayablesController(GetPayablesSummaryUseCase summary,
            SearchSupplierDebtUseCase search, GetSupplierDebtUseCase detail,
            GetSupplierPurchasesUseCase purchases,
            GetPayablePaymentHistoryUseCase paymentHistory) {
        this.summary = summary;
        this.search = search;
        this.detail = detail;
        this.purchases = purchases;
        this.paymentHistory = paymentHistory;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_LIST')")
    public PayablesSummaryResponse getSummary() {
        return summary.execute();
    }

    @GetMapping("/suppliers")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_LIST')")
    public PageResponse<SupplierDebtListResponse> getSuppliers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) SupplierDebtAction action) {
        return action == null
                ? this.search.execute(search, page, size, sort)
                : this.search.execute(search, page, size, sort, action);
    }

    @GetMapping("/suppliers/{id}")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public SupplierDebtSummaryResponse getSupplier(@PathVariable UUID id) {
        return detail.execute(id);
    }

    @GetMapping("/suppliers/{id}/purchases")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public PageResponse<SupplierPurchaseDebtResponse> getSupplierPurchases(
            @PathVariable UUID id, @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeNonDebt) {
        return purchases.execute(id, search, status, page, size,
                includeNonDebt);
    }

    @GetMapping("/payments/history")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public PageResponse<PayablePaymentHistoryResponse> getPaymentHistory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return paymentHistory.execute(search, page, size);
    }

    @GetMapping("/suppliers/{id}/payment-history")
    @PreAuthorize("hasAuthority('FINANCE_TREASURY_PAYABLES_VIEW_DEBT')")
    public PageResponse<PayablePaymentHistoryResponse> getSupplierPaymentHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID purchaseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return paymentHistory.execute(id, purchaseId, page, size);
    }
}
