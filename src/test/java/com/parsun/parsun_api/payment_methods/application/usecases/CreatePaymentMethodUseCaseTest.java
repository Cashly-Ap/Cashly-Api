package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.request.CreatePaymentMethodRequest;
import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.application.mappers.PaymentMethodMapper;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
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
@DisplayName("CreatePaymentMethodUseCase Tests")
class CreatePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private CreatePaymentMethodUseCase useCase;

    @Captor
    private ArgumentCaptor<PaymentMethod> paymentMethodCaptor;

    @Test
    void should_CreatePaymentMethod_When_ValidRequest() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Cash", "Cash payment method");

        when(repository.findMaxCodeNumber()).thenReturn(Optional.of(5));

        PaymentMethod savedPaymentMethod = PaymentMethod.reconstitute(
                PaymentMethodIdHelper.create(), "PM-0006", "Cash", "Cash payment method",
                true, LocalDateTime.now(), LocalDateTime.now());
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedPaymentMethod);

        PaymentMethodResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("PM-0006", result.code());
        assertEquals("Cash", result.name());
        assertEquals("Cash payment method", result.description());
        assertTrue(result.isActive());

        verify(repository).findMaxCodeNumber();
        verify(repository).save(paymentMethodCaptor.capture());
        PaymentMethod captured = paymentMethodCaptor.getValue();
        assertEquals("PM-0006", captured.getCode());
        assertEquals("Cash", captured.getName());
        assertEquals("Cash payment method", captured.getDescription());
        assertTrue(captured.isActive());
    }

    @Test
    void should_CreateWithFirstCode_When_NoExistingCodes() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Bank Transfer", "Bank transfer payment");

        when(repository.findMaxCodeNumber()).thenReturn(Optional.empty());

        PaymentMethod savedPaymentMethod = PaymentMethod.reconstitute(
                PaymentMethodIdHelper.create(), "PM-0001", "Bank Transfer", "Bank transfer payment",
                true, LocalDateTime.now(), LocalDateTime.now());
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedPaymentMethod);

        PaymentMethodResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("PM-0001", result.code());

        verify(repository).findMaxCodeNumber();
        verify(repository).save(any(PaymentMethod.class));
    }

    @Test
    void should_CreateWithNullDescription_When_RequestDescriptionIsNull() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Cash", null);

        when(repository.findMaxCodeNumber()).thenReturn(Optional.of(0));

        PaymentMethod savedPaymentMethod = PaymentMethod.reconstitute(
                PaymentMethodIdHelper.create(), "PM-0001", "Cash", null,
                true, LocalDateTime.now(), LocalDateTime.now());
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedPaymentMethod);

        PaymentMethodResponse result = useCase.execute(request);

        assertNotNull(result);
        assertNull(result.description());
    }

    @Test
    void should_ThrowException_When_RepositoryFails() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Cash", "Cash payment");

        when(repository.findMaxCodeNumber()).thenReturn(Optional.of(0));
        when(repository.save(any(PaymentMethod.class)))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> useCase.execute(request));

        assertEquals("Database error", exception.getMessage());
        verify(repository).save(any(PaymentMethod.class));
    }

    
    private static final class PaymentMethodIdHelper {
        static com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId create() {
            return com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId.of(
                    UUID.randomUUID());
        }
    }
}
