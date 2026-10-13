package com.cashly.cashly_api.customers.application.ports.out;
import com.cashly.cashly_api.customers.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.application.dto.response.OverdueInvoiceResponse;
import java.util.List;
import java.util.UUID;
public interface CreditValidationGateway {
    CreditStatusResponse getCreditStatus(UUID customerId);
    List<OverdueInvoiceResponse> findAllOverdueInvoices();
}
