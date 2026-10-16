package com.cashly.cashly_api.receivables.application.customer_debt;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtListResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;

import org.springframework.transaction.annotation.Transactional;

public class SearchCustomerDebtUseCase {

    private final ReceivablesQueryGateway receivablesRepository;

    public SearchCustomerDebtUseCase(ReceivablesQueryGateway receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerDebtListResponse> execute(String search, int page, int size, String sort) {
        PageResponse<CustomerDebtListResponse> result = receivablesRepository.findCustomersWithDebt(search, page, size, sort);
        return result;
    }
}
