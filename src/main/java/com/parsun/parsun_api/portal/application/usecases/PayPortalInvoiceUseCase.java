package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.payments.application.dto.request.PayInvoiceRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.payments.application.usecases.PayInvoiceUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalInvoiceRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class PayPortalInvoiceUseCase {

    private final PayInvoiceUseCase payInvoiceUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public PayPortalInvoiceUseCase(PayInvoiceUseCase payInvoiceUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.payInvoiceUseCase = payInvoiceUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional
    public PaymentResultResponse execute(UUID invoiceId, PayPortalInvoiceRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

        PayInvoiceRequest internalRequest = new PayInvoiceRequest(
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                request.amount(),
                request.useBalance(),
                request.balanceAmount()
        );

        return payInvoiceUseCase.execute(invoiceId, internalRequest, customerId, customerId.toString());
    }
}
