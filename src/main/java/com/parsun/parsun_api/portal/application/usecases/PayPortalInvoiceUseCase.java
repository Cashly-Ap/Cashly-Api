package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.CreatePaymentUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalInvoiceRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class PayPortalInvoiceUseCase {

    private static final String STATUS_PENDING = "PENDING";

    private final CreatePaymentUseCase createPaymentUseCase;
    private final PortalAuthorizationService portalAuthorizationService;
    private final NotificationService notificationService;

    public PayPortalInvoiceUseCase(CreatePaymentUseCase createPaymentUseCase,
                                    PortalAuthorizationService portalAuthorizationService,
                                    NotificationService notificationService) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentResponse execute(UUID invoiceId, PayPortalInvoiceRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();

        CreatePaymentRequest createRequest = new CreatePaymentRequest(
                customerId,
                STATUS_PENDING,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                null
        );

        PaymentResponse response = createPaymentUseCase.execute(createRequest, null);
        notificationService.notifyAdminsPaymentPending(response.id(), "Cliente", request.amount());
        return response;
    }
}
