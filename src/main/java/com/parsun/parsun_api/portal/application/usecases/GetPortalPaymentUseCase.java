package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GetPortalPaymentUseCase {

    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public GetPortalPaymentUseCase(GetPaymentByIdUseCase getPaymentByIdUseCase,
                                  PortalAuthorizationService portalAuthorizationService) {
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public PaymentResponse execute(UUID id) {
        var payment = getPaymentByIdUseCase.execute(id);
        portalAuthorizationService.requireOwnership(payment.customerId(), "Payment");
        return payment;
    }
}
