package com.cashly.cashly_api.finance.treasury.payables.application.customer_debt.use_case;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetSupplierDebtUseCase {

    private final PayablesQueryGateway gateway;

    public GetSupplierDebtUseCase(PayablesQueryGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(readOnly = true)
    public SupplierDebtSummaryResponse execute(UUID supplierId) {
        return gateway.findSupplierDebt(supplierId);
    }
}
