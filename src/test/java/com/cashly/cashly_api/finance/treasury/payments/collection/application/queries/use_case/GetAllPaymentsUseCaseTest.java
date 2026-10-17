package com.cashly.cashly_api.finance.treasury.payments.collection.application.queries.use_case;

import com.cashly.cashly_api.finance.treasury.payments.collection.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.entity.Payment;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.repository.PaymentRepository;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentNumber;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentSource;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentStatus;
import com.cashly.cashly_api.finance.treasury.payments.collection.domain.valueobjects.PaymentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllPaymentsUseCase Unit Tests")
class GetAllPaymentsUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    private GetAllPaymentsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllPaymentsUseCase(paymentRepository);
    }

    @Test
    void should_ReturnAllPayments_When_NoFilters() {
        Payment payment = buildPayment();

        when(paymentRepository.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(payment));

        List<PaymentListResponse> result = useCase.execute(null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PAY-2026-0001", result.get(0).paymentNumber());
        verify(paymentRepository).findAllWithFilters(null, null, null, null);
    }

    @Test
    void should_ReturnFilteredPayments_When_DateRangeProvided() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);

        when(paymentRepository.findAllWithFilters(dateFrom, dateTo, null, null))
                .thenReturn(List.of());

        List<PaymentListResponse> result = useCase.execute(dateFrom, dateTo, null, null, null);

        assertTrue(result.isEmpty());
        verify(paymentRepository).findAllWithFilters(dateFrom, dateTo, null, null);
    }

    @Test
    void should_UseTypeAsStatus_When_StatusIsNull() {
        String type = "APPLIED";

        when(paymentRepository.findAllWithFilters(null, null, null, type))
                .thenReturn(List.of());

        useCase.execute(null, null, null, type, null);

        verify(paymentRepository).findAllWithFilters(null, null, null, type);
    }

    @Test
    void should_UseStatus_When_BothTypeAndStatusProvided() {
        String type = "PENDING";
        String status = "APPLIED";

        when(paymentRepository.findAllWithFilters(null, null, null, status))
                .thenReturn(List.of());

        useCase.execute(null, null, null, type, status);

        verify(paymentRepository).findAllWithFilters(null, null, null, status);
    }

    @Test
    void should_ReturnFilteredPayments_When_CustomerIdProvided() {
        UUID customerId = UUID.randomUUID();

        when(paymentRepository.findAllWithFilters(null, null, customerId, null))
                .thenReturn(List.of());

        List<PaymentListResponse> result = useCase.execute(null, null, customerId, null, null);

        assertTrue(result.isEmpty());
        verify(paymentRepository).findAllWithFilters(null, null, customerId, null);
    }

    @Test
    void should_MapEachPayment_When_MultiplePayments() {
        Payment payment1 = buildPayment();
        Payment payment2 = buildPayment();

        when(paymentRepository.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(payment1, payment2));

        List<PaymentListResponse> result = useCase.execute(null, null, null, null, null);

        assertEquals(2, result.size());
        assertNotNull(result.get(0).paymentNumber());
        assertNotNull(result.get(1).paymentNumber());
        verify(paymentRepository).findAllWithFilters(null, null, null, null);
    }

    private Payment buildPayment() {
        return Payment.reconstitute(
                PaymentId.generate(),
                PaymentNumber.of(2026, 1),
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                UUID.randomUUID(),
                PaymentSource.PAYMENT_METHOD,
                "REF-001",
                LocalDate.now(),
                "Test payment",
                PaymentStatus.APPLIED,
                BigDecimal.ZERO,
                UUID.randomUUID(),
                "Test User",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
