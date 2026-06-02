package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtListResponse;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public class SearchCustomerDebtUseCase {

    private final ReceivablesRepository receivablesRepository;

    public SearchCustomerDebtUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerDebtListResponse> execute(String search, int page, int size, String sort) {
        Page<CustomerDebtListResponse> result = receivablesRepository.findCustomersWithDebt(search, page, size, sort);
        return PageResponse.from(result);
    }
}
