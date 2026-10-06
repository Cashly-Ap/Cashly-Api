package com.cashly.cashly_api.customers.application.usecases.credit;

import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.application.services.CreditValidationService;
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
