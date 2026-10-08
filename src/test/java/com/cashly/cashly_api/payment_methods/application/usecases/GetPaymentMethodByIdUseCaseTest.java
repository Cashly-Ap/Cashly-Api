package com.cashly.cashly_api.payment_methods.application.usecases;

import com.cashly.cashly_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.payment_methods.domain.entities.PaymentMethod;
import com.cashly.cashly_api.payment_methods.application.contracts.PaymentMethodUsageGateway;
import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("GetPaymentMethodByIdUseCase Tests")
class GetPaymentMethodByIdUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodUsageGateway usageGateway;

    @InjectMocks
    private GetPaymentMethodByIdUseCase useCase;

    @Test
    void should_ReturnPaymentMethod_When_Found() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod paymentMethod = PaymentMethod.reconstitute(
                paymentMethodId, "PM-0001", "Cash", "Cash payment",
                true, now, now);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.of(paymentMethod));
        when(usageGateway.hasAssociatedRecords(paymentMethodId.value())).thenReturn(false);

        PaymentMethodResponse result = useCase.execute(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("PM-0001", result.code());
        assertEquals("Cash", result.name());
        assertEquals("Cash payment", result.description());
        assertTrue(result.isActive());
        assertEquals(now, result.createdAt());
        assertEquals(now, result.updatedAt());

        verify(repository).findById(paymentMethodId);
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
    }
}
