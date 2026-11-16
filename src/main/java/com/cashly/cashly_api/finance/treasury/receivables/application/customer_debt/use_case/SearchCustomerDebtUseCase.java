package com.cashly.cashly_api.finance.treasury.receivables.application.customer_debt.use_case;

import com.cashly.cashly_api.finance.treasury.receivables.application.dto.response.CustomerDebtListResponse;
import com.cashly.cashly_api.finance.treasury.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;

import org.springframework.transaction.annotation.Transactional;

public class SearchCustomerDebtUseCase {

    private final ReceivablesQueryGateway receivablesRepository;

    public SearchCustomerDebtUseCase(
            ReceivablesQueryGateway receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerDebtListResponse> execute(String search,
            int page, int size, String sort) {
        return receivablesRepository.findCustomersWithDebt(search, page, size,
                sort);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerDebtListResponse> execute(String search,
            int page, int size, String sort, CustomerDebtAction action) {
        return action == null
                ? execute(search, page, size, sort)
                : receivablesRepository.findCustomersWithDebt(search, page,
                        size, sort, action);
    }
}
