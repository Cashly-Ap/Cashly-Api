package com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtListResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

public class SearchSupplierDebtUseCase {

    private final PayablesQueryGateway gateway;

    public SearchSupplierDebtUseCase(PayablesQueryGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierDebtListResponse> execute(String search,
            int page, int size, String sort) {
        return gateway.findSuppliersWithDebt(search, page, size, sort, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierDebtListResponse> execute(String search,
            int page, int size, String sort, SupplierDebtAction action) {
        return gateway.findSuppliersWithDebt(search, page, size, sort, action);
    }
}
