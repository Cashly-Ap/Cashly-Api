package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PendingPaymentResponse;
import com.parsun.parsun_api.finances.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetCustomerPaymentHistoryUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetCustomerPaymentHistoryUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(UUID customerId, UUID invoiceId, int page, int size) {
        Page<PendingPaymentResponse> result = receivablesRepository.findCustomerPaymentHistory(
                customerId, invoiceId, page, size);
        return PageResponse.from(result);
    }
}
