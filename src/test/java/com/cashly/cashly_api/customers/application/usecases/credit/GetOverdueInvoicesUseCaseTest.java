package com.cashly.cashly_api.customers.application.usecases.credit;

import com.cashly.cashly_api.customers.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.application.services.CreditValidationService;
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
    private CreditValidationService creditValidationService;

    private GetOverdueInvoicesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetOverdueInvoicesUseCase(creditValidationService);
    }

    @Test
    void should_ReturnOverdueInvoices() {
        List<OverdueInvoiceResponse> expectedInvoices = List.of(
                new OverdueInvoiceResponse("FAC-001", new BigDecimal("500.00"),
                        LocalDate.now().minusDays(10), 10),
                new OverdueInvoiceResponse("FAC-002", new BigDecimal("300.00"),
                        LocalDate.now().minusDays(5), 5)
        );
        when(creditValidationService.findAllOverdueInvoices()).thenReturn(expectedInvoices);

        List<OverdueInvoiceResponse> result = useCase.execute();

        assertEquals(2, result.size());
        assertEquals("FAC-001", result.get(0).invoiceNumber());
        assertEquals("FAC-002", result.get(1).invoiceNumber());
        verify(creditValidationService).findAllOverdueInvoices();
    }

    @Test
    void should_ReturnEmptyList_When_NoOverdueInvoices() {
        when(creditValidationService.findAllOverdueInvoices()).thenReturn(List.of());

        List<OverdueInvoiceResponse> result = useCase.execute();

        assertTrue(result.isEmpty());
        verify(creditValidationService).findAllOverdueInvoices();
    }
}
