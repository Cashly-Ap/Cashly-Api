package com.cashly.cashly_api.payments.collection.application.update.use_case;

import com.cashly.cashly_api.payments.collection.application.update.request.UpdatePaymentRequest;
import com.cashly.cashly_api.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.payments.collection.domain.valueobjects.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePaymentUseCase Unit Tests")
class UpdatePaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    private UpdatePaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePaymentUseCase(paymentRepository);
    }

    @Test
    void should_UpdatePayment_When_PaymentExists() {
        UUID id = UUID.randomUUID();
        Payment payment = buildPayment(id);

        when(paymentRepository.findById(PaymentId.of(id))).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePaymentRequest request = new UpdatePaymentRequest("REF-UPDATED", null, null, null);

        PaymentResponse result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals(id, result.id());
        verify(paymentRepository).findById(PaymentId.of(id));
        verify(paymentRepository).save(payment);
    }

    @Test
    void should_ThrowException_When_PaymentDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(paymentRepository.findById(PaymentId.of(id))).thenReturn(Optional.empty());

        UpdatePaymentRequest request = new UpdatePaymentRequest("REF-UPDATED", null, null, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(id, request)
        );

        assertEquals("Payment not found: " + id, exception.getMessage());
        verify(paymentRepository).findById(PaymentId.of(id));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void should_AcceptNullReferenceNumber() {
        UUID id = UUID.randomUUID();
        Payment payment = buildPayment(id);

        when(paymentRepository.findById(PaymentId.of(id))).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePaymentRequest request = new UpdatePaymentRequest(null, null, null, null);

        PaymentResponse result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals(id, result.id());
        verify(paymentRepository).save(payment);
    }

    private Payment buildPayment(UUID id) {
        return Payment.reconstitute(
                PaymentId.of(id),
                PaymentNumber.of(2026, 1),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                UUID.randomUUID(),
                PaymentSource.PAYMENT_METHOD,
                "REF-001",
                LocalDate.now(),
                "Test notes",
                PaymentStatus.APPLIED,
                BigDecimal.ZERO,
                UUID.randomUUID(),
                "Test User",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
