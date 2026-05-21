package com.parsun.parsun_api.finances.application.usecases;

import com.parsun.parsun_api.finances.application.dto.request.UpdatePaymentMethodRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePaymentMethodUseCase Tests")
class UpdatePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private UpdatePaymentMethodUseCase useCase;

    @Test
    void should_UpdatePaymentMethod_When_Found() {
        UUID id = UUID.randomUUID();
        UpdatePaymentMethodRequest request = new UpdatePaymentMethodRequest("Updated Name", "Updated Description");

        PaymentMethod existing = PaymentMethod.reconstitute(
                PaymentMethodId.of(id), "PM-0001", "Old Name", "Old Description", true,
                LocalDateTime.now(), LocalDateTime.now());

        PaymentMethod updated = PaymentMethod.reconstitute(
                PaymentMethodId.of(id), "PM-0001", "Updated Name", "Updated Description", true,
                existing.getCreatedAt(), LocalDateTime.now());

        when(repository.findById(PaymentMethodId.of(id))).thenReturn(Optional.of(existing));
        when(repository.save(any(PaymentMethod.class))).thenReturn(updated);

        PaymentMethodResponse result = useCase.execute(id, request);

        assertNotNull(result);
        assertEquals("Updated Name", result.name());
        assertEquals("Updated Description", result.description());
        verify(repository).findById(PaymentMethodId.of(id));
        verify(repository).save(any(PaymentMethod.class));
    }

    @Test
    void should_ThrowException_When_NotFound() {
        UUID id = UUID.randomUUID();
        UpdatePaymentMethodRequest request = new UpdatePaymentMethodRequest("Name", "Description");

        when(repository.findById(PaymentMethodId.of(id))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(id, request));

        verify(repository).findById(PaymentMethodId.of(id));
        verify(repository, never()).save(any());
    }
}
