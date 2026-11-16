package com.cashly.cashly_api.finance.treasury.receivables.application.summary.use_case;

import com.cashly.cashly_api.finance.treasury.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.cashly.cashly_api.finance.treasury.receivables.application.ports.out.ReceivablesQueryGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetReceivablesSummaryUseCase Unit Tests")
class GetReceivablesSummaryUseCaseTest {

    @Mock
    private ReceivablesQueryGateway receivablesRepository;

    private GetReceivablesSummaryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetReceivablesSummaryUseCase(receivablesRepository);
    }

    @Test
    void should_ReturnSummary_When_RepositoryReturnsData() {
        ReceivablesSummaryResponse expected = new ReceivablesSummaryResponse(
                new BigDecimal("50000.00"), 25L, 120L,
                new BigDecimal("3000.00"), new BigDecimal("15000.00"),
                LocalDateTime.now());

        when(receivablesRepository.findSummary()).thenReturn(expected);

        ReceivablesSummaryResponse result = useCase.execute();

        assertNotNull(result);
        assertEquals(new BigDecimal("50000.00"), result.totalDebt());
        assertEquals(25L, result.totalCustomersWithDebt());
        assertEquals(120L, result.totalInvoicesWithDebt());
        assertEquals(new BigDecimal("3000.00"),
                result.totalPendingValidationAmount());
        assertEquals(new BigDecimal("15000.00"), result.totalCustomerBalance());
        verify(receivablesRepository).findSummary();
    }

    @Test
    void should_ReturnEmptySummary_When_RepositoryReturnsEmpty() {
        ReceivablesSummaryResponse empty = ReceivablesSummaryResponse.empty();

        when(receivablesRepository.findSummary()).thenReturn(empty);

        ReceivablesSummaryResponse result = useCase.execute();

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalDebt());
        assertEquals(0L, result.totalCustomersWithDebt());
        assertEquals(0L, result.totalInvoicesWithDebt());
        assertEquals(BigDecimal.ZERO, result.totalPendingValidationAmount());
        assertEquals(BigDecimal.ZERO, result.totalCustomerBalance());
        verify(receivablesRepository).findSummary();
    }
}
