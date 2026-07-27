package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.payments.application.dto.request.PayCustomerDebtRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.application.usecases.PayAllCustomerDebtUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalDebtRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class PayPortalAllDebtUseCase {

    private final PayAllCustomerDebtUseCase payAllCustomerDebtUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public PayPortalAllDebtUseCase(PayAllCustomerDebtUseCase payAllCustomerDebtUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.payAllCustomerDebtUseCase = payAllCustomerDebtUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional
    public PaymentResultResponse execute(PayPortalDebtRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

        PayCustomerDebtRequest internalRequest = new PayCustomerDebtRequest(
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.useBalance(),
                request.balanceAmount()
        );

        return payAllCustomerDebtUseCase.execute(customerId, internalRequest, customerId, customerId.toString());
    }
}
