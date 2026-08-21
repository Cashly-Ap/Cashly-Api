package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.CreatePaymentUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalInvoiceRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public class PayPortalInvoiceUseCase {

    private static final String STATUS_PENDING = "PENDING";
    private static final String JUSTIFICANTE_PATH = "payments/justificantes/";

    private final CreatePaymentUseCase createPaymentUseCase;
    private final PortalAuthorizationService portalAuthorizationService;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public PayPortalInvoiceUseCase(CreatePaymentUseCase createPaymentUseCase,
                                    PortalAuthorizationService portalAuthorizationService,
                                    NotificationService notificationService,
                                    FileStorageService fileStorageService) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public PaymentResponse execute(UUID invoiceId, PayPortalInvoiceRequest request, MultipartFile justificante) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        String voucherPath = storeVoucherIfPresent(justificante);

        CreatePaymentRequest createRequest = new CreatePaymentRequest(
                customerId,
                STATUS_PENDING,
                request.amount(),
                request.paymentMethodId(),
                request.referenceNumber(),
                request.paymentDate(),
                null,
                invoiceId,
                voucherPath
        );

        PaymentResponse response = createPaymentUseCase.execute(createRequest, null);
        notificationService.notifyAdminsPaymentPending(response.id(), "Cliente", request.amount());
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
