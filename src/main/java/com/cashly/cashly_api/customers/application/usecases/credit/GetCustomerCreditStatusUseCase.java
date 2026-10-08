package com.cashly.cashly_api.customers.application.usecases.credit;

import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.application.contracts.CreditValidationGateway;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class GetCustomerCreditStatusUseCase {

    private final CreditValidationGateway creditValidationGateway;

    public GetCustomerCreditStatusUseCase(CreditValidationGateway creditValidationGateway) {
        this.creditValidationGateway = creditValidationGateway;
    }

    @Transactional
    public CreditStatusResponse execute(UUID customerId) {
        return creditValidationGateway.getCreditStatus(customerId);
    }
}
