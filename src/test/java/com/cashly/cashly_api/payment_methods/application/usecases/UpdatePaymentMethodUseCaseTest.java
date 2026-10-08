package com.cashly.cashly_api.payment_methods.application.usecases;

import com.cashly.cashly_api.payment_methods.application.dto.request.UpdatePaymentMethodRequest;
import com.cashly.cashly_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.payment_methods.domain.entities.PaymentMethod;
import com.cashly.cashly_api.payment_methods.application.contracts.PaymentMethodUsageGateway;
import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
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
@DisplayName("UpdatePaymentMethodUseCase Tests")
class UpdatePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodUsageGateway usageGateway;

    @InjectMocks
    private UpdatePaymentMethodUseCase useCase;

    @Captor
    private ArgumentCaptor<PaymentMethod> paymentMethodCaptor;

    @Test
    void should_UpdatePaymentMethod_When_ValidRequest() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod existing = PaymentMethod.reconstitute(
                paymentMethodId, "PM-0001", "Cash", "Cash payment",
                true, now, now);

        UpdatePaymentMethodRequest request = new UpdatePaymentMethodRequest(
                "Credit Card", "Credit card payment", false);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.of(existing));

        PaymentMethod saved = PaymentMethod.reconstitute(
                paymentMethodId, "PM-0001", "Credit Card", "Credit card payment",
                false, now, LocalDateTime.now());
        when(repository.save(any(PaymentMethod.class))).thenReturn(saved);
        when(usageGateway.hasAssociatedRecords(any())).thenReturn(false);

        PaymentMethodResponse result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals("Credit Card", result.name());
        assertEquals("Credit card payment", result.description());
        assertFalse(result.isActive());

        verify(repository).findById(paymentMethodId);
        verify(repository).save(paymentMethodCaptor.capture());
        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertEquals("Credit Card", captured.getName());
        assertEquals("Credit card payment", captured.getDescription());
        assertFalse(captured.isActive());
    }

    @Test
    void should_NotChangeActive_When_IsActiveIsNull() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);
        LocalDateTime now = LocalDateTime.now();

        PaymentMethod existing = PaymentMethod.reconstitute(
                paymentMethodId, "PM-0001", "Cash", "Cash payment",
                true, now, now);

        UpdatePaymentMethodRequest request = new UpdatePaymentMethodRequest(
                "Updated Cash", "Updated description", null);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.of(existing));
        when(repository.save(any(PaymentMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usageGateway.hasAssociatedRecords(any())).thenReturn(false);

        PaymentMethodResponse result = useCase.execute(id, request);

        assertTrue(result.isActive());
        assertEquals("Updated Cash", result.name());
    }

    @Test
    void should_ThrowException_When_NotFound() {
        UUID id = UUID.randomUUID();
        PaymentMethodId paymentMethodId = PaymentMethodId.of(id);

        UpdatePaymentMethodRequest request = new UpdatePaymentMethodRequest(
                "Credit Card", "Credit card payment", true);

        when(repository.findById(paymentMethodId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(id, request));

        assertTrue(exception.getMessage().contains("Payment method not found"));
        assertTrue(exception.getMessage().contains(id.toString()));
        verify(repository).findById(paymentMethodId);
        verify(repository, never()).save(any());
    }
}
