package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.ReceivablesSummaryResponse;
import com.parsun.parsun_api.finances.domain.repositories.ReceivablesRepository;
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
