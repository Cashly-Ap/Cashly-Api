package com.cashly.cashly_api.customers.credit.application.overdue;

import com.cashly.cashly_api.customers.credit.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.credit.application.ports.out.CreditValidationGateway;
import jakarta.transaction.Transactional;

import java.util.List;

public class GetOverdueInvoicesUseCase {

    private final CreditValidationGateway creditValidationGateway;

    public GetOverdueInvoicesUseCase(CreditValidationGateway creditValidationGateway) {
        this.creditValidationGateway = creditValidationGateway;
    }

    @Transactional
    public List<OverdueInvoiceResponse> execute() {
        return creditValidationGateway.findAllOverdueInvoices();
    }
}
