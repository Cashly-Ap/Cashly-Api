package com.cashly.cashly_api.finance.treasury.receivables.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.receivables.application.dto.response.CustomerInvoiceDebtResponse;
import com.cashly.cashly_api.finance.treasury.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.cashly.cashly_api.finance.treasury.receivables.application.customer_debt.use_case.GetCustomerDebtUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.customer_invoices.use_case.GetCustomerInvoicesUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.payment_history.use_case.GetPaymentHistoryUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.summary.use_case.GetReceivablesSummaryUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.application.customer_debt.use_case.SearchCustomerDebtUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceivablesController Unit Tests")
class ReceivablesControllerTest {

    @Mock
    private GetReceivablesSummaryUseCase getReceivablesSummaryUseCase;

    @Mock
    private SearchCustomerDebtUseCase searchCustomerDebtUseCase;

    @Mock
    private GetCustomerDebtUseCase getCustomerDebtUseCase;

    @Mock
    private GetCustomerInvoicesUseCase getCustomerInvoicesUseCase;

    @Mock
    private GetPaymentHistoryUseCase getPaymentHistoryUseCase;

    @InjectMocks
    private ReceivablesController receivablesController;

    @Test
    @DisplayName("should get summary and return 200")
    void should_get_summary_and_return_200() {
        ReceivablesSummaryResponse response = new ReceivablesSummaryResponse(
                BigDecimal.TEN, 5L, 10L, BigDecimal.ZERO, BigDecimal.ZERO,
                LocalDateTime.now());

        when(getReceivablesSummaryUseCase.execute()).thenReturn(response);

        ResponseEntity<ReceivablesSummaryResponse> result = receivablesController
                .getSummary();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BigDecimal.TEN, result.getBody().totalDebt());
        verify(getReceivablesSummaryUseCase).execute();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("should get customers with debt and return 200")
    void should_get_customers_with_debt_and_return_200() {
        PageResponse<?> pageResponse = new PageResponse<>(List.of(), 0L, 0, 20,
                0, true, true, true);

        doReturn(pageResponse).when(searchCustomerDebtUseCase).execute(any(),
                anyInt(), anyInt(), any());

        ResponseEntity<PageResponse<?>> result = receivablesController
                .getCustomersWithDebt(null, 0, 20, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(searchCustomerDebtUseCase).execute(null, 0, 20, null);
    }

    @Test
    @DisplayName("should get customer debt and return 200")
    void should_get_customer_debt_and_return_200() {
        UUID id = UUID.randomUUID();
        CustomerDebtSummaryResponse response = new CustomerDebtSummaryResponse(
                id, "Customer", "J-123", null, null, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.valueOf(100),
                BigDecimal.valueOf(90), BigDecimal.ZERO, List.of(), 0L);

        when(getCustomerDebtUseCase.execute(id)).thenReturn(response);

        ResponseEntity<CustomerDebtSummaryResponse> result = receivablesController
                .getCustomerDebt(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().customerId());
        verify(getCustomerDebtUseCase).execute(id);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("should get customer invoices and return 200")
    void should_get_customer_invoices_and_return_200() {
        UUID customerId = UUID.randomUUID();
        PageResponse<?> pageResponse = new PageResponse<>(List.of(), 0L, 0, 20,
                0, true, true, true);

        doReturn(pageResponse).when(getCustomerInvoicesUseCase)
                .execute(eq(customerId), any(), any(), anyInt(), anyInt());

        ResponseEntity<PageResponse<?>> result = receivablesController
                .getCustomerInvoices(customerId, null, null, 0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(getCustomerInvoicesUseCase).execute(customerId, null, null, 0,
                20);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("should get payment history and return 200")
    void should_get_payment_history_and_return_200() {
        PageResponse<?> pageResponse = new PageResponse<>(List.of(), 0L, 0, 20,
                0, true, true, true);

        doReturn(pageResponse).when(getPaymentHistoryUseCase).execute(any(),
                anyInt(), anyInt());

        ResponseEntity<PageResponse<?>> result = receivablesController
                .getPaymentHistory(null, 0, 20);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(getPaymentHistoryUseCase).execute(null, 0, 20);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("should get customer payment history and return 200")
    void should_get_customer_payment_history_and_return_200() {
        UUID customerId = UUID.randomUUID();
        PageResponse<?> pageResponse = new PageResponse<>(List.of(), 0L, 0, 100,
                0, true, true, true);

        doReturn(pageResponse).when(getPaymentHistoryUseCase)
                .execute(eq(customerId), any(), anyInt(), anyInt());

        ResponseEntity<PageResponse<?>> result = receivablesController
                .getCustomerPaymentHistory(customerId, null, 0, 100);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(getPaymentHistoryUseCase).execute(customerId, null, 0, 100);
    }
}
