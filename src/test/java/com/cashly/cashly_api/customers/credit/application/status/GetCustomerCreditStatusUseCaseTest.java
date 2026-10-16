package com.cashly.cashly_api.customers.credit.application.status;

import com.cashly.cashly_api.customers.credit.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.credit.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.credit.application.ports.out.CreditValidationGateway;
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
@DisplayName("GetCustomerCreditStatusUseCase Unit Tests")
class GetCustomerCreditStatusUseCaseTest {

    @Mock
    private CreditValidationGateway creditValidationGateway;

    private GetCustomerCreditStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCustomerCreditStatusUseCase(creditValidationGateway);
    }

    @Test
    void should_ReturnCreditStatus() {
        UUID customerId = UUID.randomUUID();
        CreditStatusResponse expected = new CreditStatusResponse(
                customerId, "Empresa Test", "J-12345678-9",
                new BigDecimal("5000.00"), new BigDecimal("1500.00"),
                new BigDecimal("200.00"), new BigDecimal("3700.00"),
                List.of(), false, true
        );
        when(creditValidationGateway.getCreditStatus(customerId)).thenReturn(expected);

        CreditStatusResponse result = useCase.execute(customerId);

        assertEquals(customerId, result.customerId());
        assertEquals("Empresa Test", result.customerName());
        assertEquals(new BigDecimal("5000.00"), result.creditLimit());
        assertEquals(new BigDecimal("3700.00"), result.availableCredit());
        assertTrue(result.canCreateCreditInvoice());
        verify(creditValidationGateway).getCreditStatus(customerId);
    }

    @Test
    void should_ReturnCreditStatus_WithOverdueInvoices() {
        UUID customerId = UUID.randomUUID();
        List<OverdueInvoiceResponse> overdue = List.of(
                new OverdueInvoiceResponse("FAC-001", new BigDecimal("500.00"),
                        LocalDate.now().minusDays(10), 10)
        );
        CreditStatusResponse expected = new CreditStatusResponse(
                customerId, "Empresa Test", "J-12345678-9",
                new BigDecimal("5000.00"), new BigDecimal("1500.00"),
                BigDecimal.ZERO, new BigDecimal("3500.00"),
                overdue, true, false
        );
        when(creditValidationGateway.getCreditStatus(customerId)).thenReturn(expected);

        CreditStatusResponse result = useCase.execute(customerId);

        assertTrue(result.hasOverdue());
        assertFalse(result.canCreateCreditInvoice());
        assertEquals(1, result.overdueInvoices().size());
        verify(creditValidationGateway).getCreditStatus(customerId);
    }
}
