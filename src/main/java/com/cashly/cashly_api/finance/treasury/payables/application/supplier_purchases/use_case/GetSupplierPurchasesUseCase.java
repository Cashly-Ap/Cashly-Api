package com.cashly.cashly_api.finance.treasury.payables.application.supplier_purchases.use_case;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierPurchaseDebtResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetSupplierPurchasesUseCase {

    private final PayablesQueryGateway gateway;

    public GetSupplierPurchasesUseCase(PayablesQueryGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierPurchaseDebtResponse> execute(UUID supplierId,
            String search, String status, int page, int size,
            boolean includeNonDebt) {
        return gateway.findSupplierPurchases(supplierId, search, status, page,
                size, includeNonDebt);
    }
}
