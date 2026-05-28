package com.parsun.parsun_api.receivables.application.usecases;

import com.parsun.parsun_api.receivables.application.dto.response.PendingPaymentResponse;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public class GetPendingPaymentsUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetPendingPaymentsUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(String status, String search, int page, int size) {
        Page<PendingPaymentResponse> result = receivablesRepository.findPendingPayments(status, search, page, size);
        return PageResponse.from(result);
    }
}
