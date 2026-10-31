package com.cashly.cashly_api.finance.treasury.payments.collection.application.delete.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePaymentUseCase Unit Tests")
class DeletePaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    private DeletePaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePaymentUseCase(paymentRepository);
    }

    @Test
    void should_DeletePayment_When_PaymentExists() {
        UUID id = UUID.randomUUID();
        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(PaymentId.of(id)))
                .thenReturn(Optional.of(payment));

        useCase.execute(id);

        verify(paymentRepository).findById(PaymentId.of(id));
        verify(paymentRepository).deleteById(PaymentId.of(id));
    }

    @Test
    void should_ThrowException_When_PaymentDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(paymentRepository.findById(PaymentId.of(id)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> useCase.execute(id));

        assertEquals("Payment not found: " + id, exception.getMessage());
        verify(paymentRepository).findById(PaymentId.of(id));
        verify(paymentRepository, never()).deleteById(any());
    }

    @Test
    void should_DeleteCorrectPayment_When_MultiplePaymentsExist() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Payment payment = mock(Payment.class);

        when(paymentRepository.findById(PaymentId.of(id1)))
                .thenReturn(Optional.of(payment));

        useCase.execute(id1);

        verify(paymentRepository).deleteById(PaymentId.of(id1));
        verify(paymentRepository, never()).deleteById(PaymentId.of(id2));
    }
}
