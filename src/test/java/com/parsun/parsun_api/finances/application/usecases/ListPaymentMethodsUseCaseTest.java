package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

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
    void should_ReturnAllPaymentMethods_When_ActiveOnlyIsFalse() {
        PaymentMethod m1 = PaymentMethod.reconstitute(
                com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId.generate(),
                "PM-0001", "Efectivo", "Cash", true, LocalDateTime.now(), LocalDateTime.now());
        PaymentMethod m2 = PaymentMethod.reconstitute(
                com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId.generate(),
                "PM-0002", "Transferencia", "Bank transfer", false, LocalDateTime.now(), LocalDateTime.now());

        when(repository.findAll(false)).thenReturn(List.of(m1, m2));

        List<PaymentMethodResponse> result = useCase.execute(false);

        assertEquals(2, result.size());
        verify(repository).findAll(false);
    }

    @Test
    void should_ReturnOnlyActivePaymentMethods_When_ActiveOnlyIsTrue() {
        PaymentMethod m1 = PaymentMethod.reconstitute(
                com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId.generate(),
                "PM-0001", "Efectivo", "Cash", true, LocalDateTime.now(), LocalDateTime.now());

        when(repository.findAll(true)).thenReturn(List.of(m1));

        List<PaymentMethodResponse> result = useCase.execute(true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(repository).findAll(true);
    }

    @Test
    void should_ReturnEmptyList_When_NoMethodsExist() {
        when(repository.findAll(false)).thenReturn(List.of());

        List<PaymentMethodResponse> result = useCase.execute(false);

        assertTrue(result.isEmpty());
    }
}
