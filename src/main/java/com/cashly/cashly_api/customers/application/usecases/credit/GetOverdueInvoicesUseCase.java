package com.cashly.cashly_api.customers.application.usecases.credit;

import com.cashly.cashly_api.customers.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.application.services.CreditValidationService;
import jakarta.transaction.Transactional;

import java.util.List;

public class GetOverdueInvoicesUseCase {

    private final CreditValidationService creditValidationService;

    public GetOverdueInvoicesUseCase(CreditValidationService creditValidationService) {
        this.creditValidationService = creditValidationService;
    }

    @Transactional
    public List<OverdueInvoiceResponse> execute() {
        return creditValidationService.findAllOverdueInvoices();
    }
}
