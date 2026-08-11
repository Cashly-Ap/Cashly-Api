package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.CreatePaymentUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalDebtRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class PayPortalAllDebtUseCase {

    private static final String STATUS_PENDING = "PENDING";

    private final CreatePaymentUseCase createPaymentUseCase;
    private final PortalAuthorizationService portalAuthorizationService;
    private final NotificationService notificationService;
    private final CustomerRepository customerRepository;

    public PayPortalAllDebtUseCase(CreatePaymentUseCase createPaymentUseCase,
                                    PortalAuthorizationService portalAuthorizationService,
                                    NotificationService notificationService,
                                    CustomerRepository customerRepository) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.notificationService = notificationService;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public PaymentResponse execute(PayPortalDebtRequest request) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        String customerName = customerRepository.findById(new CustomerId(customerId))
                .map(c -> c.getLegalName().getValue())
                .orElse("Cliente");

        CreatePaymentRequest createRequest = new CreatePaymentRequest(
                customerId,
                STATUS_PENDING,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                "Pago total de deuda"
        );

        PaymentResponse response = createPaymentUseCase.execute(createRequest, null);
        notificationService.notifyAdminsPaymentPending(response.id(), customerName, request.amount());
        return response;
    }
}
