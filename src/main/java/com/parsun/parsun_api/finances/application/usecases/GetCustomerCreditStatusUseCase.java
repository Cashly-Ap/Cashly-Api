package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.finances.domain.services.CreditValidationService;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class GetCustomerCreditStatusUseCase {

    private final CreditValidationService creditValidationService;

    public GetCustomerCreditStatusUseCase(CreditValidationService creditValidationService) {
        this.creditValidationService = creditValidationService;
    }

    @Transactional
    public CreditStatusResponse execute(UUID customerId) {
        return creditValidationService.getCreditStatus(customerId);
    }
}
