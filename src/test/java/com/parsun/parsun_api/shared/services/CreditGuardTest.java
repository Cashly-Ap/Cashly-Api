package com.parsun.parsun_api.shared.services;

import com.parsun.parsun_api.customers.application.dto.response.CreditStatusResponse;
import com.parsun.parsun_api.customers.application.services.CreditValidationService;
import com.parsun.parsun_api.shared.services.exceptions.CreditLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditGuard Unit Tests")
class CreditGuardTest {

    @Mock
    private CreditValidationService creditValidationService;

    private CreditGuard creditGuard;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        creditGuard = new CreditGuard(creditValidationService);
    }

    @Test
    void should_ReturnCreditStatus_When_GetCreditStatusCalled() {
        CreditStatusResponse expected = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), false, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(expected);

        CreditStatusResponse result = creditGuard.getCreditStatus(customerId);

        assertSame(expected, result);
        verify(creditValidationService).getCreditStatus(customerId);
    }

    @Test
    void should_ReturnTrue_When_CreditIsAvailable() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), false, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        boolean result = creditGuard.isCreditAvailable(customerId, new BigDecimal("5000.00"));

        assertTrue(result);
        verify(creditValidationService).getCreditStatus(customerId);
    }

    @Test
    void should_ReturnFalse_When_CustomerHasOverdueInvoices() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), true, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        boolean result = creditGuard.isCreditAvailable(customerId, new BigDecimal("5000.00"));

        assertFalse(result);
    }

    @Test
    void should_ReturnFalse_When_CannotCreateCreditInvoice() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), false, false
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        boolean result = creditGuard.isCreditAvailable(customerId, new BigDecimal("5000.00"));

        assertFalse(result);
    }

    @Test
    void should_ReturnFalse_When_InvoiceExceedsAvailableCredit() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), false, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        boolean result = creditGuard.isCreditAvailable(customerId, new BigDecimal("8000.00"));

        assertFalse(result);
    }

    @Test
    void should_ThrowException_When_HasOverdueInvoices() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), true, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        CreditLimitExceededException exception = assertThrows(
                CreditLimitExceededException.class,
                () -> creditGuard.ensureCreditAvailable(customerId, new BigDecimal("5000.00"))
        );

        assertTrue(exception.getMessage().contains("tiene facturas vencidas"));
        verify(creditValidationService).getCreditStatus(customerId);
    }

    @Test
    void should_ThrowException_When_CannotCreateCreditInvoice() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), false, false
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        CreditLimitExceededException exception = assertThrows(
                CreditLimitExceededException.class,
                () -> creditGuard.ensureCreditAvailable(customerId, new BigDecimal("5000.00"))
        );

        assertTrue(exception.getMessage().contains("no puede crear facturas a crédito"));
    }

    @Test
    void should_ThrowException_When_CreditInsufficient() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("8000.00"),
                BigDecimal.ZERO, new BigDecimal("2000.00"),
                List.of(), false, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        CreditLimitExceededException exception = assertThrows(
                CreditLimitExceededException.class,
                () -> creditGuard.ensureCreditAvailable(customerId, new BigDecimal("5000.00"))
        );

        assertTrue(exception.getMessage().contains("Crédito insuficiente"));
        assertTrue(exception.getMessage().contains("2000"));
    }

    @Test
    void should_NotThrow_When_CreditIsAvailable() {
        CreditStatusResponse status = new CreditStatusResponse(
                customerId, "Cliente Test", "J-12345678-9",
                new BigDecimal("10000.00"), new BigDecimal("3000.00"),
                BigDecimal.ZERO, new BigDecimal("7000.00"),
                List.of(), false, true
        );

        when(creditValidationService.getCreditStatus(customerId)).thenReturn(status);

        assertDoesNotThrow(() ->
                creditGuard.ensureCreditAvailable(customerId, new BigDecimal("5000.00"))
        );

        verify(creditValidationService).getCreditStatus(customerId);
    }
}
