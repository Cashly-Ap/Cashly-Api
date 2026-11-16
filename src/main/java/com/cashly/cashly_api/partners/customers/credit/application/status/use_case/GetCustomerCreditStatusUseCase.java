package com.cashly.cashly_api.partners.customers.credit.application.status.use_case;

import com.cashly.cashly_api.partners.customers.credit.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.partners.customers.credit.application.ports.out.CreditValidationGateway;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class GetCustomerCreditStatusUseCase {

    private final CreditValidationGateway creditValidationGateway;

    public GetCustomerCreditStatusUseCase(
            CreditValidationGateway creditValidationGateway) {
        this.creditValidationGateway = creditValidationGateway;
    }

    @Transactional
    public CreditStatusResponse execute(UUID customerId) {
        return creditValidationGateway.getCreditStatus(customerId);
    }
}
