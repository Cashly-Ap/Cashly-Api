package com.cashly.cashly_api.receivables.application.payment_history;

import com.cashly.cashly_api.receivables.application.dto.response.PendingPaymentResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPaymentHistoryUseCase {

    private final ReceivablesQueryGateway receivablesRepository;

    public GetPaymentHistoryUseCase(ReceivablesQueryGateway receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(String search, int page, int size) {
        PageResponse<PendingPaymentResponse> result = receivablesRepository.findPaymentHistory(search, page, size);
        return result;
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(UUID customerId, UUID invoiceId, int page, int size) {
        PageResponse<PendingPaymentResponse> result = receivablesRepository.findCustomerPaymentHistory(customerId, invoiceId, page, size);
        return result;
    }
}
