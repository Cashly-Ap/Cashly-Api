package com.parsun.parsun_api.payment_methods.application.usecases;

import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payment_methods.domain.valueobjects.PaymentMethodId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPaymentMethodsUseCase Tests")
class ListPaymentMethodsUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private ListPaymentMethodsUseCase useCase;

    @Test
    void should_ReturnAllPaymentMethods_When_ActiveFilterIsNull() {
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod pm1 = PaymentMethod.reconstitute(
                PaymentMethodId.of(UUID.randomUUID()), "PM-0001", "Cash", "Cash payment",
                true, now, now);
        PaymentMethod pm2 = PaymentMethod.reconstitute(
                PaymentMethodId.of(UUID.randomUUID()), "PM-0002", "Credit Card", "Credit card payment",
                true, now, now);

        when(repository.findAll(null)).thenReturn(List.of(pm1, pm2));

        List<PaymentMethodResponse> result = useCase.execute(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PM-0001", result.get(0).code());
        assertEquals("PM-0002", result.get(1).code());

        verify(repository).findAll(null);
    }

    @Test
    void should_ReturnActivePaymentMethods_When_ActiveFilterIsTrue() {
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod pm = PaymentMethod.reconstitute(
                PaymentMethodId.of(UUID.randomUUID()), "PM-0001", "Cash", "Cash payment",
                true, now, now);

        when(repository.findAll(true)).thenReturn(List.of(pm));

        List<PaymentMethodResponse> result = useCase.execute(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());

        verify(repository).findAll(true);
    }

    @Test
    void should_ReturnInactivePaymentMethods_When_ActiveFilterIsFalse() {
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod pm = PaymentMethod.reconstitute(
                PaymentMethodId.of(UUID.randomUUID()), "PM-0002", "Credit Card", "Inactive",
                false, now, now);

        when(repository.findAll(false)).thenReturn(List.of(pm));

        List<PaymentMethodResponse> result = useCase.execute(false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isActive());

        verify(repository).findAll(false);
    }

    @Test
    void should_ReturnEmptyList_When_NoPaymentMethods() {
        when(repository.findAll(null)).thenReturn(List.of());

        List<PaymentMethodResponse> result = useCase.execute(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(repository).findAll(null);
    }
}
