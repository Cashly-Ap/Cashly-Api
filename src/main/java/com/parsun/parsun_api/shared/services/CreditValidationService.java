package com.parsun.parsun_api.shared.services;

import com.parsun.parsun_api.receivables.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.receivables.application.dto.response.OverdueInvoiceResponse;

import java.util.List;
import java.util.UUID;

public interface CreditValidationService {

    CreditStatusResponse getCreditStatus(UUID customerId);

    List<OverdueInvoiceResponse> findAllOverdueInvoices();
}
