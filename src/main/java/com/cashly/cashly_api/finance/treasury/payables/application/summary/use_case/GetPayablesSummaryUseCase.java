package com.cashly.cashly_api.finance.treasury.payables.application.summary.use_case;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.PayablesSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import org.springframework.transaction.annotation.Transactional;

public class GetPayablesSummaryUseCase {

    private final PayablesQueryGateway gateway;

    public GetPayablesSummaryUseCase(PayablesQueryGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional(readOnly = true)
    public PayablesSummaryResponse execute() {
        return gateway.findSummary();
    }
}
