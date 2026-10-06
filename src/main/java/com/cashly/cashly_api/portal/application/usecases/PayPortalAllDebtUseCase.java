package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.payments.application.dto.request.CreatePaymentRequest;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.application.usecases.CreatePaymentUseCase;
import com.cashly.cashly_api.portal.application.dto.request.PayPortalDebtRequest;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import com.cashly.cashly_api.shared.storage.domain.FileStorageService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public class PayPortalAllDebtUseCase {

    private static final String STATUS_PENDING = "PENDING";
    private static final String JUSTIFICANTE_PATH = "payments/justificantes/";

    private final CreatePaymentUseCase createPaymentUseCase;
    private final PortalAuthorizationService portalAuthorizationService;
    private final NotificationService notificationService;
    private final CustomerRepository customerRepository;
    private final FileStorageService fileStorageService;

    public PayPortalAllDebtUseCase(CreatePaymentUseCase createPaymentUseCase,
                                    PortalAuthorizationService portalAuthorizationService,
                                    NotificationService notificationService,
                                    CustomerRepository customerRepository,
                                    FileStorageService fileStorageService) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.notificationService = notificationService;
        this.customerRepository = customerRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public PaymentResponse execute(PayPortalDebtRequest request, MultipartFile justificante) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        String customerName = customerRepository.findById(new CustomerId(customerId))
                .map(c -> c.getLegalName().getValue())
                .orElse("Cliente");
        String voucherPath = storeVoucherIfPresent(justificante);

        CreatePaymentRequest createRequest = new CreatePaymentRequest(
                customerId,
                STATUS_PENDING,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                "Pago total de deuda",
                null,
                voucherPath
        );

        PaymentResponse response = createPaymentUseCase.execute(createRequest, null);
        notificationService.notifyAdminsPaymentPending(response.id(), customerName, request.amount());
        return response;
    }

    private String storeVoucherIfPresent(MultipartFile justificante) {
        if (justificante == null || justificante.isEmpty()) {
            return null;
        }
        try {
            return fileStorageService.store(
                    JUSTIFICANTE_PATH + UUID.randomUUID() + "_" + justificante.getOriginalFilename(),
                    justificante.getInputStream(),
                    justificante.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Error storing justificante file", e);
        }
    }
}
