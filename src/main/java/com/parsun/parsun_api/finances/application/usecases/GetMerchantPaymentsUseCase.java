package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PendingPaymentResponse;
import com.parsun.parsun_api.finances.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetMerchantPaymentsUseCase {

    private final ReceivablesRepository receivablesRepository;

    public GetMerchantPaymentsUseCase(ReceivablesRepository receivablesRepository) {
        this.receivablesRepository = receivablesRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PendingPaymentResponse> execute(UUID merchantId, String status, String search,
                                                         int page, int size) {
        Page<PendingPaymentResponse> result = receivablesRepository.findMerchantPayments(
                merchantId, status, search, page, size);
        return PageResponse.from(result);
    }
}
