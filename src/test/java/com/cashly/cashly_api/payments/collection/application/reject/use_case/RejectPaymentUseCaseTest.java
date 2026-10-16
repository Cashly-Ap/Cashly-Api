package com.cashly.cashly_api.payments.collection.application.reject.use_case;

import com.cashly.cashly_api.payments.collection.application.ports.out.PaymentNotificationGateway;
import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.payments.collection.application.reject.request.RejectPaymentRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RejectPaymentUseCaseTest {
    @Test
    void rejection_is_saved_before_customer_notification_is_sent() {
        PaymentRepository payments = mock(PaymentRepository.class);
        PaymentNotificationGateway notifications = mock(PaymentNotificationGateway.class);
        RejectPaymentUseCase useCase = new RejectPaymentUseCase(payments, notifications);
        UUID customerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Payment payment = Payment.create(PaymentNumber.of(2026, 1), customerId,
                new BigDecimal("75.00"), UUID.randomUUID(), "REF-1", LocalDate.now(),
                null, null, null, PaymentStatus.PENDING);
        String reason = "Comprobante no válido";
        when(payments.findById(PaymentId.of(payment.getId().value()))).thenReturn(Optional.of(payment));
        when(payments.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(payment.getId().value(), new RejectPaymentRequest(reason), actorId, "Admin");

        assertEquals(PaymentStatus.REJECTED.name(), response.status());
        verify(payments).save(payment);
        verify(notifications).rejected(customerId, payment.getId().value(), reason);
    }
}
