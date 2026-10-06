package com.cashly.cashly_api.portal.application.usecases;

import com.cashly.cashly_api.payments.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.domain.entities.Payment;
import com.cashly.cashly_api.payments.domain.repositories.PaymentRepository;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.payments.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPendingPortalPaymentsUseCase Unit Tests")
class GetPendingPortalPaymentsUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PortalAuthorizationService portalAuthorizationService;

    private GetPendingPortalPaymentsUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID otherInvoiceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetPendingPortalPaymentsUseCase(paymentRepository, portalAuthorizationService);
        when(portalAuthorizationService.getCurrentCustomerId()).thenReturn(customerId);
    }

    private Payment pendingPayment(UUID targetInvoiceId) {
        return Payment.create(
                PaymentNumber.of(2026, (int) (Math.random() * 10000)),
                customerId,
                new BigDecimal("100.00"),
                UUID.randomUUID(),
                "REF",
                LocalDate.now(),
                null,
                UUID.randomUUID(),
                "Test",
                PaymentStatus.PENDING,
                targetInvoiceId,
                null
        );
    }

    @Test
    void should_IncludeTargetedPayment_When_QueryingByThatInvoiceId() {
        Payment targeted = pendingPayment(invoiceId);
        when(paymentRepository.findAllWithFilters(null, null, customerId, "PENDING"))
                .thenReturn(List.of(targeted));

        List<PaymentListResponse> result = useCase.execute(invoiceId);

        assertEquals(1, result.size());
    }

    @Test
    void should_IncludeGeneralDebtPayment_When_QueryingByAnyInvoiceId() {
        Payment generalDebt = pendingPayment(null);
        when(paymentRepository.findAllWithFilters(null, null, customerId, "PENDING"))
                .thenReturn(List.of(generalDebt));

        List<PaymentListResponse> result = useCase.execute(invoiceId);

        assertEquals(1, result.size());
    }

    @Test
    void should_ExcludePaymentTargetingDifferentInvoice_When_QueryingByInvoiceId() {
        Payment otherTargeted = pendingPayment(otherInvoiceId);
        when(paymentRepository.findAllWithFilters(null, null, customerId, "PENDING"))
                .thenReturn(List.of(otherTargeted));

        List<PaymentListResponse> result = useCase.execute(invoiceId);

        assertTrue(result.isEmpty());
    }

    @Test
    void should_OnlyIncludeGeneralDebtPayments_When_QueryingWithoutInvoiceId() {
        Payment generalDebt = pendingPayment(null);
        Payment targeted = pendingPayment(invoiceId);
        when(paymentRepository.findAllWithFilters(null, null, customerId, "PENDING"))
                .thenReturn(List.of(generalDebt, targeted));

        List<PaymentListResponse> result = useCase.execute(null);

        assertEquals(1, result.size());
    }
}
