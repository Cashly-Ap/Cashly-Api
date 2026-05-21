package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.OverdueInvoiceResponse;
import com.parsun.parsun_api.finances.domain.services.CreditValidationService;
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
