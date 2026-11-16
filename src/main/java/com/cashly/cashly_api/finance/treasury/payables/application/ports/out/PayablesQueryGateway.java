package com.cashly.cashly_api.finance.treasury.payables.application.ports.out;

import com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case.SupplierDebtAction;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablePaymentHistoryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablesSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtListResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierPurchaseDebtResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface PayablesQueryGateway {
    PayablesSummaryResponse findSummary();
    PageResponse<SupplierDebtListResponse> findSuppliersWithDebt(String search,
            int page, int size, String sort, SupplierDebtAction action);
    SupplierDebtSummaryResponse findSupplierDebt(UUID supplierId);
    PageResponse<SupplierPurchaseDebtResponse> findSupplierPurchases(
            UUID supplierId, String search, String status, int page, int size,
            boolean includeNonDebt);
    PageResponse<PayablePaymentHistoryResponse> findPaymentHistory(
            String search, int page, int size);
    PageResponse<PayablePaymentHistoryResponse> findSupplierPaymentHistory(
            UUID supplierId, UUID purchaseId, int page, int size);
    BigDecimal findSupplierBalance(UUID supplierId);
}
