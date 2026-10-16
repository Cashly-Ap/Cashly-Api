package com.cashly.cashly_api.receivables.application.summary.use_case;

import com.cashly.cashly_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import org.springframework.transaction.annotation.Transactional;

public class GetReceivablesSummaryUseCase {

    private final ReceivablesQueryGateway receivablesRepository;

    public GetReceivablesSummaryUseCase(ReceivablesQueryGateway receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public ReceivablesSummaryResponse execute() {
        return receivablesRepository.findSummary();
    }
}
