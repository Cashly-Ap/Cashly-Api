package com.cashly.cashly_api.payment_methods.application.delete.use_case;

import com.cashly.cashly_api.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import com.cashly.cashly_api.payment_methods.domain.repository.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePaymentMethodUseCase Unit Tests")
class DeletePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodUsageGateway usageGateway;

    private DeletePaymentMethodUseCase useCase;

    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DeletePaymentMethodUseCase(repository, usageGateway);
    }

    @Test
    void should_DeletePaymentMethod_When_Exists() {
        when(repository.findById(PaymentMethodId.of(paymentMethodId)))
                .thenReturn(Optional.of(mock(com.cashly.cashly_api.payment_methods.domain.entity.PaymentMethod.class)));
        when(usageGateway.hasAssociatedRecords(paymentMethodId))
                .thenReturn(false);

        assertDoesNotThrow(() -> useCase.execute(paymentMethodId));
        verify(repository).delete(PaymentMethodId.of(paymentMethodId));
    }

    @Test
    void should_Throw_When_HasAssociatedRecords() {
        when(repository.findById(PaymentMethodId.of(paymentMethodId)))
                .thenReturn(Optional.of(mock(com.cashly.cashly_api.payment_methods.domain.entity.PaymentMethod.class)));
        when(usageGateway.hasAssociatedRecords(paymentMethodId))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(paymentMethodId)
        );

        assertEquals("No se puede eliminar el método de pago porque tiene pagos o cierres de caja asociados.",
                exception.getMessage());
        verify(repository, never()).delete(any());
    }

    @Test
    void should_Throw_When_PaymentMethodNotFound() {
        when(repository.findById(PaymentMethodId.of(paymentMethodId)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(paymentMethodId)
        );

        assertEquals("Payment method not found: " + paymentMethodId, exception.getMessage());
    }
}
