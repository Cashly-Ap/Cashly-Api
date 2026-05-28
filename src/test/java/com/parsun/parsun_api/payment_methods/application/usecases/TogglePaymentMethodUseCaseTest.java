package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TogglePaymentMethodUseCase Tests")
class TogglePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private TogglePaymentMethodUseCase useCase;

    @Captor
    private ArgumentCaptor<PaymentMethod> paymentMethodCaptor;

    @Test
    void should_Deactivate_When_CurrentlyActive() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod existing = PaymentMethod.reconstitute(
                paymentMethodId, "PM-0001", "Cash", "Cash payment",
                true, now, now);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.of(existing));
        when(repository.save(any(PaymentMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentMethodResponse result = useCase.execute(id);

        assertNotNull(result);
        assertFalse(result.isActive());

        verify(repository).findById(paymentMethodId);
        verify(repository).save(paymentMethodCaptor.capture());
        assertFalse(paymentMethodCaptor.getValue().isActive());
    }

    @Test
    void should_Activate_When_CurrentlyInactive() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod existing = PaymentMethod.reconstitute(
                paymentMethodId, "PM-0001", "Cash", "Cash payment",
                false, now, now);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.of(existing));
        when(repository.save(any(PaymentMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentMethodResponse result = useCase.execute(id);

        assertNotNull(result);
        assertTrue(result.isActive());

        verify(repository).findById(paymentMethodId);
        verify(repository).save(paymentMethodCaptor.capture());
        assertTrue(paymentMethodCaptor.getValue().isActive());
    }

    @Test
    void should_ThrowException_When_NotFound() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(id));

        assertTrue(exception.getMessage().contains("Payment method not found"));
        assertTrue(exception.getMessage().contains(id.toString()));
        verify(repository).findById(paymentMethodId);
        verify(repository, never()).save(any());
    }
}
