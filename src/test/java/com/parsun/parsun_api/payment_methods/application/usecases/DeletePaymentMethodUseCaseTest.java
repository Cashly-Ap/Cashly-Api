package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletePaymentMethodUseCase Unit Tests")
class DeletePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    private DeletePaymentMethodUseCase useCase;

    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DeletePaymentMethodUseCase(repository);
    }

    @Test
    void should_DeletePaymentMethod_When_Exists() {
        when(repository.findById(PaymentMethodId.of(paymentMethodId)))
                .thenReturn(Optional.of(mock(com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod.class)));

        assertDoesNotThrow(() -> useCase.execute(paymentMethodId));
        verify(repository).delete(PaymentMethodId.of(paymentMethodId));
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
