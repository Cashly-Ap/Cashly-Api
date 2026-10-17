package com.cashly.cashly_api.finance.treasury.receivables.application.payment_history.use_case;

import com.cashly.cashly_api.finance.treasury.receivables.application.dto.response.PendingPaymentResponse;
import com.cashly.cashly_api.finance.treasury.receivables.application.ports.out.ReceivablesQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPaymentHistoryUseCase Unit Tests")
class GetPaymentHistoryUseCaseTest {

    @Mock
    private ReceivablesQueryGateway receivablesRepository;

    private GetPaymentHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPaymentHistoryUseCase(receivablesRepository);
    }

    @Test
    void should_ReturnPaymentHistory_When_ExecutedWithoutCustomerFilter() {
        PendingPaymentResponse payment = new PendingPaymentResponse(
                UUID.randomUUID(), "PAY-2026-00001", UUID.randomUUID(), "Cliente Test",
                new BigDecimal("500.00"), "Efectivo", "APPLIED", "PAYMENT_METHOD",
                LocalDate.now(), LocalDateTime.now(), "INV-2026-00001"
        );
        PageResponse<PendingPaymentResponse> paymentPage = toPageResponse(new PageImpl<>(
                List.of(payment), PageRequest.of(0, 20), 1
        ));

        when(receivablesRepository.findPaymentHistory(null, 0, 20)).thenReturn(paymentPage);

        PageResponse<PendingPaymentResponse> result = useCase.execute(null, 0, 20);

        assertNotNull(result);
        assertFalse(result.empty());
        assertEquals(1, result.content().size());
        assertEquals("PAY-2026-00001", result.content().get(0).paymentNumber());
        assertEquals("APPLIED", result.content().get(0).status());
        verify(receivablesRepository).findPaymentHistory(null, 0, 20);
    }

    @Test
    void should_ReturnFilteredPaymentHistory_When_SearchProvided() {
        PendingPaymentResponse payment = new PendingPaymentResponse(
                UUID.randomUUID(), "PAY-2026-00001", UUID.randomUUID(), "Cliente Test",
                new BigDecimal("500.00"), "Efectivo", "APPLIED", "PAYMENT_METHOD",
                LocalDate.now(), LocalDateTime.now(), "INV-2026-00001"
        );
        PageResponse<PendingPaymentResponse> paymentPage = toPageResponse(new PageImpl<>(
                List.of(payment), PageRequest.of(0, 10), 1
        ));

        when(receivablesRepository.findPaymentHistory("PAY-2026", 0, 10)).thenReturn(paymentPage);

        PageResponse<PendingPaymentResponse> result = useCase.execute("PAY-2026", 0, 10);

        assertEquals(1, result.content().size());
        verify(receivablesRepository).findPaymentHistory("PAY-2026", 0, 10);
    }

    @Test
    void should_ReturnCustomerPaymentHistory_When_CustomerIdProvided() {
        UUID customerId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        PendingPaymentResponse payment = new PendingPaymentResponse(
                UUID.randomUUID(), "PAY-2026-00001", customerId, "Cliente Test",
                new BigDecimal("500.00"), "Transferencia", "APPLIED", "PAYMENT_METHOD",
                LocalDate.now(), LocalDateTime.now(), "INV-2026-00001"
        );
        PageResponse<PendingPaymentResponse> paymentPage = toPageResponse(new PageImpl<>(
                List.of(payment), PageRequest.of(0, 100), 1
        ));

        when(receivablesRepository.findCustomerPaymentHistory(customerId, invoiceId, 0, 100))
                .thenReturn(paymentPage);

        PageResponse<PendingPaymentResponse> result = useCase.execute(customerId, invoiceId, 0, 100);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(customerId, result.content().get(0).customerId());
        verify(receivablesRepository).findCustomerPaymentHistory(customerId, invoiceId, 0, 100);
    }

    @Test
    void should_ReturnCustomerPaymentHistory_When_OnlyCustomerIdProvided() {
        UUID customerId = UUID.randomUUID();

        PageResponse<PendingPaymentResponse> paymentPage = toPageResponse(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        when(receivablesRepository.findCustomerPaymentHistory(customerId, null, 0, 100))
                .thenReturn(paymentPage);

        PageResponse<PendingPaymentResponse> result = useCase.execute(customerId, null, 0, 100);

        assertTrue(result.empty());
        assertEquals(0, result.totalElements());
        verify(receivablesRepository).findCustomerPaymentHistory(customerId, null, 0, 100);
    }

    @Test
    void should_ReturnEmptyHistory_When_NoPaymentsExist() {
        PageResponse<PendingPaymentResponse> emptyPage = toPageResponse(org.springframework.data.domain.Page.empty());

        when(receivablesRepository.findPaymentHistory(null, 0, 20)).thenReturn(emptyPage);

        PageResponse<PendingPaymentResponse> result = useCase.execute(null, 0, 20);

        assertTrue(result.empty());
        assertEquals(0, result.totalElements());
        verify(receivablesRepository).findPaymentHistory(null, 0, 20);
    }

    @Test
    void should_ReturnCorrectPagination() {
        PendingPaymentResponse payment = new PendingPaymentResponse(
                UUID.randomUUID(), "PAY-2026-00001", UUID.randomUUID(), "Cliente Test",
                new BigDecimal("500.00"), "Efectivo", "APPLIED", "PAYMENT_METHOD",
                LocalDate.now(), LocalDateTime.now(), "INV-2026-00001"
        );
        PageResponse<PendingPaymentResponse> paymentPage = toPageResponse(new PageImpl<>(
                List.of(payment), PageRequest.of(2, 10), 35
        ));

        when(receivablesRepository.findPaymentHistory(null, 2, 10)).thenReturn(paymentPage);

        PageResponse<PendingPaymentResponse> result = useCase.execute(null, 2, 10);

        assertEquals(1, result.content().size());
        assertEquals(35, result.totalElements());
        assertEquals(4, result.totalPages());
        assertEquals(2, result.number());
        assertFalse(result.first());
    }
    private static <T> PageResponse<T> toPageResponse(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getSize(), page.getNumber(), page.isFirst(), page.isLast(), page.isEmpty());
    }
}
