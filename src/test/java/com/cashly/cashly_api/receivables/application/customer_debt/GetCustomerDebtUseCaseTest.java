package com.cashly.cashly_api.receivables.application.customer_debt;

import com.cashly.cashly_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.cashly.cashly_api.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.cashly.cashly_api.receivables.application.ports.out.ReceivablesQueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCustomerDebtUseCase Unit Tests")
class GetCustomerDebtUseCaseTest {

    @Mock
    private ReceivablesQueryGateway receivablesRepository;

    private GetCustomerDebtUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerDebtUseCase(receivablesRepository);
    }

    @Test
    void should_ReturnCustomerDebt_When_CustomerExists() {
        UUID customerId = UUID.randomUUID();
        CustomerDebtSummaryResponse expected = new CustomerDebtSummaryResponse(
                customerId, "Cliente Test", "J-12345678-9",
                "04121234567", "cliente@test.com",
                new BigDecimal("5000.00"), new BigDecimal("1000.00"),
                new BigDecimal("5000.00"), new BigDecimal("10000.00"),
                new BigDecimal("5000.00"), new BigDecimal("1000.00"),
                List.of(), 2L
        );

        when(receivablesRepository.findCustomerDebt(customerId)).thenReturn(expected);

        CustomerDebtSummaryResponse result = useCase.execute(customerId);

        assertNotNull(result);
        assertEquals(customerId, result.customerId());
        assertEquals("Cliente Test", result.customerName());
        assertEquals(new BigDecimal("5000.00"), result.totalDebt());
        assertEquals(new BigDecimal("10000.00"), result.creditLimit());
        verify(receivablesRepository).findCustomerDebt(customerId);
    }

    @Test
    void should_ReturnCustomerDebtWithInvoices_When_CustomerHasInvoices() {
        UUID customerId = UUID.randomUUID();
        CustomerInvoiceDebtResponse invoiceDebt = new CustomerInvoiceDebtResponse(
                UUID.randomUUID(), "INV-2026-00001", "PENDING_PAYMENT",
                LocalDate.now(), new BigDecimal("1000.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                null, null
        );
        CustomerDebtSummaryResponse expected = new CustomerDebtSummaryResponse(
                customerId, "Cliente Test", "J-12345678-9",
                null, null,
                new BigDecimal("1000.00"), BigDecimal.ZERO,
                new BigDecimal("9000.00"), new BigDecimal("10000.00"),
                new BigDecimal("9000.00"), BigDecimal.ZERO,
                List.of(invoiceDebt), 0L
        );

        when(receivablesRepository.findCustomerDebt(customerId)).thenReturn(expected);

        CustomerDebtSummaryResponse result = useCase.execute(customerId);

        assertEquals(1, result.invoicesWithDebt().size());
        assertEquals("INV-2026-00001", result.invoicesWithDebt().get(0).invoiceNumber());
        assertEquals(new BigDecimal("1000.00"), result.invoicesWithDebt().get(0).balance());
        verify(receivablesRepository).findCustomerDebt(customerId);
    }

    @Test
    void should_ReturnZeroDebt_When_CustomerHasNoDebt() {
        UUID customerId = UUID.randomUUID();
        CustomerDebtSummaryResponse expected = new CustomerDebtSummaryResponse(
                customerId, "Cliente Test", "J-12345678-9",
                null, null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("10000.00"), new BigDecimal("10000.00"),
                new BigDecimal("10000.00"), BigDecimal.ZERO,
                List.of(), 0L
        );

        when(receivablesRepository.findCustomerDebt(customerId)).thenReturn(expected);

        CustomerDebtSummaryResponse result = useCase.execute(customerId);

        assertEquals(BigDecimal.ZERO, result.totalDebt());
        assertEquals(new BigDecimal("10000.00"), result.availableCredit());
        assertTrue(result.invoicesWithDebt().isEmpty());
        verify(receivablesRepository).findCustomerDebt(customerId);
    }
}
