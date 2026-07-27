package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.GetPaymentByIdUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalPaymentUseCase {

    private final GetPaymentByIdUseCase getPaymentByIdUseCase;

    public GetPortalPaymentUseCase(GetPaymentByIdUseCase getPaymentByIdUseCase) {
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
    }

    @Transactional(readOnly = true)
    public PaymentResponse execute(UUID id) {
        return getPaymentByIdUseCase.execute(id);
    }
}
