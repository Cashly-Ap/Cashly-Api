package com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;

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
@DisplayName("GetPaymentByIdUseCase Unit Tests")
class GetPaymentByIdUseCaseUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    private GetPaymentByIdUseCase getPaymentByIdUseCase;

    @BeforeEach
    void setUp() {
        getPaymentByIdUseCase = new GetPaymentByIdUseCase(paymentRepository);
    }

    @Test
    void should_ReturnPaymentResponse_When_PaymentExists() {
        UUID id = UUID.randomUUID();
        Payment payment = buildPayment(id);

        when(paymentRepository.findById(PaymentId.of(id)))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = getPaymentByIdUseCase.execute(id);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals(payment.getPaymentNumber().value(),
                response.paymentNumber());
        assertEquals(payment.getCustomerId(), response.customerId());
        assertEquals(payment.getAmount(), response.amount());
        assertEquals(payment.getPaymentMethodId(), response.paymentMethodId());
        assertEquals(payment.getReferenceNumber(), response.referenceNumber());
        assertEquals(payment.getPaymentDate(), response.paymentDate());
        assertEquals(payment.getNotes(), response.notes());
        assertEquals(payment.getStatus().name(), response.status());
        assertEquals(payment.getAppliedAmount(), response.appliedAmount());
        assertEquals(payment.getCreatedBy(), response.createdBy());
        assertEquals(payment.getCreatedByName(), response.createdByName());
        assertEquals(payment.getCreatedAt(), response.createdAt());
        assertEquals(payment.getUpdatedAt(), response.updatedAt());

        verify(paymentRepository).findById(PaymentId.of(id));
    }

    @Test
    void should_ThrowIllegalArgumentException_When_PaymentDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(paymentRepository.findById(PaymentId.of(id)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> getPaymentByIdUseCase.execute(id));

        assertEquals("Payment not found: " + id, exception.getMessage());
        verify(paymentRepository).findById(PaymentId.of(id));
    }

    private Payment buildPayment(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return Payment.reconstitute(PaymentId.of(id), PaymentNumber.of(2026, 1),
                UUID.randomUUID(), new BigDecimal("500.00"), UUID.randomUUID(),
                PaymentSource.PAYMENT_METHOD, "REF-001",
                LocalDate.of(2026, 5, 28), "Test notes", PaymentStatus.APPLIED,
                BigDecimal.ZERO, UUID.randomUUID(), "Test User", now, now);
    }
}
