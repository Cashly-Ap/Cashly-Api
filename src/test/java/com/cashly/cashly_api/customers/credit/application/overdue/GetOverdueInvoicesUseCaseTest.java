package com.cashly.cashly_api.customers.credit.application.overdue;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOverdueInvoicesUseCase Unit Tests")
class GetOverdueInvoicesUseCaseTest {

    @Mock
    private CreditValidationGateway creditValidationGateway;

    private GetOverdueInvoicesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOverdueInvoicesUseCase(creditValidationGateway);
    }

    @Test
    void should_ReturnOverdueInvoices() {
        List<OverdueInvoiceResponse> expectedInvoices = List.of(
                new OverdueInvoiceResponse("FAC-001", new BigDecimal("500.00"),
                        LocalDate.now().minusDays(10), 10),
                new OverdueInvoiceResponse("FAC-002", new BigDecimal("300.00"),
                        LocalDate.now().minusDays(5), 5)
        );
        when(creditValidationGateway.findAllOverdueInvoices()).thenReturn(expectedInvoices);

        List<OverdueInvoiceResponse> result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals("FAC-001", result.get(0).invoiceNumber());
        assertEquals("FAC-002", result.get(1).invoiceNumber());
        verify(creditValidationGateway).findAllOverdueInvoices();
    }

    @Test
    void should_ReturnEmptyList_When_NoOverdueInvoices() {
        when(creditValidationGateway.findAllOverdueInvoices()).thenReturn(List.of());

        List<OverdueInvoiceResponse> result = useCase.execute();

        assertTrue(result.isEmpty());
        verify(creditValidationGateway).findAllOverdueInvoices();
    }
}
