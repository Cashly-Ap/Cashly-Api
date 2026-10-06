package com.cashly.cashly_api.receivables.application.usecases;

import com.cashly.cashly_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.cashly.cashly_api.receivables.domain.repositories.ReceivablesRepository;
import org.springframework.transaction.annotation.Transactional;

public class GetReceivablesSummaryUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetReceivablesSummaryUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public ReceivablesSummaryResponse execute() {
        return receivablesRepository.findSummary();
    }
}
