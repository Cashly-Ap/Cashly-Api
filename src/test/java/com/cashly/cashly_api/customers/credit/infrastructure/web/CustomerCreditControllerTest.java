package com.cashly.cashly_api.customers.credit.infrastructure.web;

import com.cashly.cashly_api.customers.credit.application.dto.response.CreditStatusResponse;
import com.cashly.cashly_api.customers.credit.application.dto.response.OverdueInvoiceResponse;
import com.cashly.cashly_api.customers.credit.application.status.GetCustomerCreditStatusUseCase;
import com.cashly.cashly_api.customers.credit.application.overdue.GetOverdueInvoicesUseCase;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerCreditController Unit Tests")
class CustomerCreditControllerTest {

    @Mock
    private GetCustomerCreditStatusUseCase getCustomerCreditStatusUseCase;

    @Mock
    private GetOverdueInvoicesUseCase getOverdueInvoicesUseCase;

    @InjectMocks
    private CustomerCreditController customerCreditController;

    @Test
    @DisplayName("should get credit status and return 200")
    void should_get_credit_status_and_return_200() {
        UUID customerId = UUID.randomUUID();
        CreditStatusResponse response = new CreditStatusResponse(
                customerId, "Test Corp", "J-12345678-9",
                BigDecimal.valueOf(10000), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.valueOf(10000), List.of(), false, true);

        when(getCustomerCreditStatusUseCase.execute(customerId)).thenReturn(response);

        ResponseEntity<CreditStatusResponse> result = customerCreditController.getCreditStatus(customerId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(customerId, result.getBody().customerId());
        assertTrue(result.getBody().canCreateCreditInvoice());
        verify(getCustomerCreditStatusUseCase).execute(customerId);
    }

    @Test
    @DisplayName("should return credit with overdue invoices")
    void should_return_credit_with_overdue_invoices() {
        UUID customerId = UUID.randomUUID();
        List<OverdueInvoiceResponse> overdue = List.of(
                new OverdueInvoiceResponse("INV-001", BigDecimal.valueOf(500), LocalDate.now().minusDays(30), 30));
        CreditStatusResponse response = new CreditStatusResponse(
                customerId, "Test Corp", "J-12345678-9",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(500), BigDecimal.ZERO,
                BigDecimal.valueOf(9500), overdue, true, false);

        when(getCustomerCreditStatusUseCase.execute(customerId)).thenReturn(response);

        ResponseEntity<CreditStatusResponse> result = customerCreditController.getCreditStatus(customerId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().hasOverdue());
        assertFalse(result.getBody().canCreateCreditInvoice());
        assertEquals(1, result.getBody().overdueInvoices().size());
        verify(getCustomerCreditStatusUseCase).execute(customerId);
    }

    @Test
    @DisplayName("should get overdue invoices and return 200")
    void should_get_overdue_invoices_and_return_200() {
        List<OverdueInvoiceResponse> response = List.of(
                new OverdueInvoiceResponse("INV-001", BigDecimal.valueOf(500), LocalDate.now().minusDays(30), 30),
                new OverdueInvoiceResponse("INV-002", BigDecimal.valueOf(250), LocalDate.now().minusDays(15), 15));

        when(getOverdueInvoicesUseCase.execute()).thenReturn(response);

        ResponseEntity<List<OverdueInvoiceResponse>> result = customerCreditController.getOverdueInvoices();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
        verify(getOverdueInvoicesUseCase).execute();
    }

    @Test
    @DisplayName("should get empty overdue invoices list")
    void should_get_empty_overdue_invoices_list() {
        when(getOverdueInvoicesUseCase.execute()).thenReturn(List.of());

        ResponseEntity<List<OverdueInvoiceResponse>> result = customerCreditController.getOverdueInvoices();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getOverdueInvoicesUseCase).execute();
    }
}
