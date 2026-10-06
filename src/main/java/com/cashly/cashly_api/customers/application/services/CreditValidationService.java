package com.cashly.cashly_api.customers.application.services;

import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.application.dto.response.OverdueInvoiceResponse;

import java.util.List;
import java.util.UUID;

public interface CreditValidationService {

    CreditStatusResponse getCreditStatus(UUID customerId);

    List<OverdueInvoiceResponse> findAllOverdueInvoices();
}
