package com.cashly.cashly_api.payment_methods.application.queries.use_case;

import com.cashly.cashly_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.cashly.cashly_api.payment_methods.domain.entity.PaymentMethod;
import com.cashly.cashly_api.payment_methods.application.ports.out.PaymentMethodUsageGateway;
import com.cashly.cashly_api.payment_methods.domain.repository.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
import com.cashly.cashly_api.shared.dto.PageResponse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPaymentMethodsUseCase Tests")
class ListPaymentMethodsUseCaseTest {

    @Mock
    private PaymentMethodRepository repository;

    @Mock
    private PaymentMethodUsageGateway usageGateway;

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

        PageResponse<PaymentMethod> pageResult = new PageResponse<>(
                List.of(pm1, pm2), 2, 1, 10, 0, true, true, false);
        when(repository.findAllWithFilters(isNull(), isNull(), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(pageResult);
        when(usageGateway.hasAssociatedRecords(any())).thenReturn(false);

        PageResponse<PaymentMethodResponse> result = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("PM-0001", result.content().get(0).code());
        assertEquals("PM-0002", result.content().get(1).code());
        assertEquals(2, result.totalElements());
    }

    @Test
    void should_ReturnActivePaymentMethods_When_ActiveFilterIsTrue() {
        LocalDateTime now = LocalDateTime.now();
        PaymentMethod pm = PaymentMethod.reconstitute(
                PaymentMethodId.of(UUID.randomUUID()), "PM-0001", "Cash", "Cash payment",
                true, now, now);

        PageResponse<PaymentMethod> pageResult = new PageResponse<>(
                List.of(pm), 1, 1, 10, 0, true, true, false);
        when(repository.findAllWithFilters(isNull(), eq(true), eq(0), eq(10), eq("name"), eq("asc")))
                .thenReturn(pageResult);
        when(usageGateway.hasAssociatedRecords(any())).thenReturn(false);

        PageResponse<PaymentMethodResponse> result = useCase.execute(null, true, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertTrue(result.content().get(0).isActive());
    }

    @Test
    void should_ReturnEmptyPage_When_NoPaymentMethods() {
        PageResponse<PaymentMethod> pageResult = new PageResponse<>(
                List.of(), 0, 0, 10, 0, true, true, true);
        when(repository.findAllWithFilters(any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(pageResult);

        PageResponse<PaymentMethodResponse> result = useCase.execute(null, null, 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }
}
