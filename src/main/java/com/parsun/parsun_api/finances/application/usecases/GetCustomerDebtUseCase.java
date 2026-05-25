package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CustomerDebtSummaryResponse;
import com.parsun.parsun_api.finances.domain.repositories.ReceivablesRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetCustomerDebtUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetCustomerDebtUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public CustomerDebtSummaryResponse execute(UUID customerId) {
        return receivablesRepository.findCustomerDebt(customerId);
    }
}
