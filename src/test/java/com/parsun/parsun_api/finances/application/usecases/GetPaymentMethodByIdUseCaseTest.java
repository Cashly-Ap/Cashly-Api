package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.finances.application.mappers.FinanceMapper;
import com.parsun.parsun_api.finances.domain.entities.PaymentMethod;
import com.parsun.parsun_api.finances.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.finances.domain.valueobjects.PaymentMethodId;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPaymentMethodByIdUseCase Tests")
class GetPaymentMethodByIdUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private GetPaymentMethodByIdUseCase useCase;

    @Test
    void should_ReturnPaymentMethod_When_Found() {
        UUID id = UUID.randomUUID();
        PaymentMethod method = PaymentMethod.reconstitute(
                PaymentMethodId.of(id), "PM-0001", "Efectivo", "Cash", true,
                LocalDateTime.now(), LocalDateTime.now());

        when(repository.findById(PaymentMethodId.of(id))).thenReturn(Optional.of(method));

        PaymentMethodResponse result = useCase.execute(id);

        assertNotNull(result);
        assertEquals("PM-0001", result.code());
        assertEquals("Efectivo", result.name());
        assertEquals("Cash", result.description());
        assertTrue(result.isActive());
        verify(repository).findById(PaymentMethodId.of(id));
    }

    @Test
    void should_ThrowException_When_NotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(PaymentMethodId.of(id))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(id));

        verify(repository).findById(PaymentMethodId.of(id));
    }
}
