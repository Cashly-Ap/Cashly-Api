package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.payments.application.dto.request.PayPartialDebtRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.application.usecases.PayPartialDebtUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalPartialDebtRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class PayPortalPartialDebtUseCase {

    private final PayPartialDebtUseCase payPartialDebtUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public PayPortalPartialDebtUseCase(PayPartialDebtUseCase payPartialDebtUseCase,
                                        PortalAuthorizationService portalAuthorizationService) {
        this.payPartialDebtUseCase = payPartialDebtUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional
    public PaymentResultResponse execute(PayPortalPartialDebtRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

        PayPartialDebtRequest internalRequest = new PayPartialDebtRequest(
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.amount(),
                request.useBalance(),
                request.balanceAmount()
        );

        return payPartialDebtUseCase.execute(customerId, internalRequest, null, null);
    }
}
