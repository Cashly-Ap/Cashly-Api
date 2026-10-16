package com.cashly.cashly_api.receivables.application.customer_invoices.use_case;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetCustomerInvoicesUseCase {

    private final ReceivablesQueryGateway receivablesRepository;

    public GetCustomerInvoicesUseCase(ReceivablesQueryGateway receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerInvoiceDebtResponse> execute(UUID customerId, String search, String status, int page, int size) {
        PageResponse<CustomerInvoiceDebtResponse> result = receivablesRepository.findCustomerInvoices(customerId, search, status, page, size);
        return result;
    }
}
