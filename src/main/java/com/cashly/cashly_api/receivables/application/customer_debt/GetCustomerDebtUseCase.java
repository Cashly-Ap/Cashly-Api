package com.cashly.cashly_api.receivables.application.customer_debt;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetCustomerDebtUseCase {

    private final ReceivablesQueryGateway receivablesRepository;

    public GetCustomerDebtUseCase(ReceivablesQueryGateway receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public CustomerDebtSummaryResponse execute(UUID customerId) {
        return receivablesRepository.findCustomerDebt(customerId);
    }
}
