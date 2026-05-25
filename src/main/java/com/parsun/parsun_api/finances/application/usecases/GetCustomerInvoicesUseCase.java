package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.CustomerInvoiceDebtResponse;
import com.parsun.parsun_api.finances.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetCustomerInvoicesUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetCustomerInvoicesUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerInvoiceDebtResponse> execute(UUID customerId, String search, String status, int page, int size) {
        Page<CustomerInvoiceDebtResponse> result = receivablesRepository.findCustomerInvoices(customerId, search, status, page, size);
        return PageResponse.from(result);
    }
}
