package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.payments.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.application.mappers.PaymentMapper;
import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class GetPendingPortalPaymentsUseCase {

    private static final String STATUS_PENDING = "PENDING";

    private final PaymentRepository paymentRepository;
    private final PortalAuthorizationService portalAuthorizationService;

    public GetPendingPortalPaymentsUseCase(PaymentRepository paymentRepository,
                                            PortalAuthorizationService portalAuthorizationService) {
        this.paymentRepository = paymentRepository;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<PaymentListResponse> execute(UUID invoiceId) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        List<Payment> pendingPayments = paymentRepository.findAllWithFilters(null, null, customerId, STATUS_PENDING);

        return pendingPayments.stream()
                .filter(payment -> appliesToQuery(payment, invoiceId))
                .map(PaymentMapper::toListResponse)
                .toList();
    }

    private boolean appliesToQuery(Payment payment, UUID invoiceId) {
        UUID targetInvoiceId = payment.getTargetInvoiceId();
        if (invoiceId == null) {
            return targetInvoiceId == null;
        }
        return targetInvoiceId == null || invoiceId.equals(targetInvoiceId);
    }
}
