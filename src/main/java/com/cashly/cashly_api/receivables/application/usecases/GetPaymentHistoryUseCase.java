package com.cashly.cashly_api.receivables.application.usecases;

import com.cashly.cashly_api.receivables.application.dto.response.PendingPaymentResponse;
import com.cashly.cashly_api.receivables.domain.repositories.ReceivablesRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPaymentHistoryUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetPaymentHistoryUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(String search, int page, int size) {
        Page<PendingPaymentResponse> result = receivablesRepository.findPaymentHistory(search, page, size);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(UUID customerId, UUID invoiceId, int page, int size) {
        Page<PendingPaymentResponse> result = receivablesRepository.findCustomerPaymentHistory(customerId, invoiceId, page, size);
        return PageResponse.from(result);
    }
}
