package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.CreatePaymentMethodRequest;
import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.infrastructure.persistence.repositories.PaymentMethodJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePaymentMethodUseCase Tests")
class CreatePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodJpaRepository jpaRepository;

    @InjectMocks
    private CreatePaymentMethodUseCase useCase;

    @Test
    void should_CreatePaymentMethod_When_ValidRequest() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Efectivo", "Pago en efectivo");

        PaymentMethod savedMethod = PaymentMethod.reconstitute(
                com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId.generate(),
                "PM-0001", "Efectivo", "Pago en efectivo", true,
                LocalDateTime.now(), LocalDateTime.now());

        when(jpaRepository.findMaxCodeNumber()).thenReturn(Optional.of(0));
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedMethod);

        PaymentMethodResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("PM-0001", result.code());
        assertEquals("Efectivo", result.name());
        assertEquals("Pago en efectivo", result.description());
        assertTrue(result.isActive());
        verify(jpaRepository).findMaxCodeNumber();
        verify(repository).save(any(PaymentMethod.class));
    }

    @Test
    void should_GenerateNextCode_When_ExistingMethodsExist() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Nuevo Metodo", "Descripcion");

        PaymentMethod savedMethod = PaymentMethod.reconstitute(
                com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId.generate(),
                "PM-0009", "Nuevo Metodo", "Descripcion", true,
                LocalDateTime.now(), LocalDateTime.now());

        when(jpaRepository.findMaxCodeNumber()).thenReturn(Optional.of(8));
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedMethod);

        PaymentMethodResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("PM-0009", result.code());
        verify(jpaRepository).findMaxCodeNumber();
    }

    @Test
    void should_GenerateCode0001_When_NoExistingMethods() {
        CreatePaymentMethodRequest request = new CreatePaymentMethodRequest("Primer Metodo", null);

        PaymentMethod savedMethod = PaymentMethod.reconstitute(
                com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId.generate(),
                "PM-0001", "Primer Metodo", null, true,
                LocalDateTime.now(), LocalDateTime.now());

        when(jpaRepository.findMaxCodeNumber()).thenReturn(Optional.empty());
        when(repository.save(any(PaymentMethod.class))).thenReturn(savedMethod);

        PaymentMethodResponse result = useCase.execute(request);

        assertNotNull(result);
        assertEquals("PM-0001", result.code());
    }
}
