package com.parsun.parsun_api.cashregister.domain.entities;

import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashRegister Domain Entity Unit Tests")
class CashRegisterUnitTest {

    private static final LocalDate CLOSURE_DATE = LocalDate.of(2026, 6, 9);
    private static final UUID OPENED_BY = UUID.randomUUID();
    private static final String OPENED_BY_NAME = "Operator Test";
    private static final RegisterId CLOSURE_ID = RegisterId.generate();

    

    @Test
    void should_OpenClosure_When_ValidDataProvided() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);

        assertEquals(CLOSURE_ID, closure.getId());
        assertEquals(CLOSURE_DATE, closure.getClosureDate());
        assertEquals(RegisterStatus.OPEN, closure.getStatus());
        assertEquals(OPENED_BY, closure.getOpenedBy());
        assertEquals(OPENED_BY_NAME, closure.getOpenedByName());
        assertNotNull(closure.getOpenedAt());
        assertNull(closure.getClosedBy());
        assertNull(closure.getClosedByName());
        assertNull(closure.getClosedAt());
        assertEquals(BigDecimal.ZERO, closure.getTotalPayments());
        assertEquals(BigDecimal.ZERO, closure.getTotalAdvances());
        assertEquals(BigDecimal.ZERO, closure.getTotalReturns());
        assertEquals(BigDecimal.ZERO, closure.getTotalCreditNotes());
        assertEquals(BigDecimal.ZERO, closure.getTotalDebitNotes());
        assertEquals(BigDecimal.ZERO, closure.getTotalInvoices());
        assertEquals(0, closure.getPaymentCount());
        assertEquals(0, closure.getAdvanceCount());
        assertEquals(0, closure.getReturnCount());
        assertEquals(0, closure.getInvoiceCount());
        assertEquals(BigDecimal.ZERO, closure.getExpectedClosingBalance());
        assertNull(closure.getActualClosingBalance());
        assertNull(closure.getDifference());
        assertNull(closure.getClosingNotes());
        assertEquals(0, closure.getVersion());
        assertNotNull(closure.getCreatedAt());
        assertNotNull(closure.getUpdatedAt());
        assertTrue(closure.getDetails().isEmpty());
    }

    @Test
    void should_ThrowException_When_RegisterIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                CashRegister.open(null, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME));
    }

    @Test
    void should_ThrowException_When_ClosureDateIsNull() {
        assertThrows(NullPointerException.class, () ->
                CashRegister.open(CLOSURE_ID, null, OPENED_BY, OPENED_BY_NAME));
    }

    @Test
    void should_ThrowException_When_OpenedByIsNull() {
        assertThrows(NullPointerException.class, () ->
                CashRegister.open(CLOSURE_ID, CLOSURE_DATE, null, OPENED_BY_NAME));
    }

    @Test
    void should_ThrowException_When_OpenedByNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, null));
    }

    @Test
    void should_ThrowException_When_OpenedByNameIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, "   "));
        assertEquals("Opened by name must not be blank", exception.getMessage());
    }

    

    @Test
    void should_ApplyTotals_When_ValidDataProvided() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);

        List<CashRegisterDetail> details = List.of(
                CashRegisterDetail.create(UUID.randomUUID(), "Efectivo", 5, new BigDecimal("150.00")),
                CashRegisterDetail.create(UUID.randomUUID(), "Transferencia", 3, new BigDecimal("2000.00"))
        );

        closure.applyTotals(
                new BigDecimal("2150.00"), 5,   
                new BigDecimal("500.00"), 1,    
                new BigDecimal("100.00"), 2,    
                new BigDecimal("50.00"),        
                new BigDecimal("0"),            
                new BigDecimal("3500.00"), 3,   
                details
        );

        assertEquals(new BigDecimal("2150.00"), closure.getTotalPayments());
        assertEquals(new BigDecimal("500.00"), closure.getTotalAdvances());
        assertEquals(new BigDecimal("100.00"), closure.getTotalReturns());
        assertEquals(new BigDecimal("50.00"), closure.getTotalCreditNotes());
        assertEquals(new BigDecimal("0"), closure.getTotalDebitNotes());
        assertEquals(new BigDecimal("3500.00"), closure.getTotalInvoices());
        assertEquals(5, closure.getPaymentCount());
        assertEquals(1, closure.getAdvanceCount());
        assertEquals(2, closure.getReturnCount());
        assertEquals(3, closure.getInvoiceCount());

        
        
        assertEquals(new BigDecimal("2500.00"), closure.getExpectedClosingBalance());
        assertEquals(2, closure.getDetails().size());
    }

    @Test
    void should_ApplyTotals_Even_When_ClosureIsNotOpen() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);
        closure.close(UUID.randomUUID(), "Test", null);

        
        closure.applyTotals(
                new BigDecimal("100.00"), 1,
                BigDecimal.ZERO, 0,
                BigDecimal.ZERO, 0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO, 0,
                Collections.emptyList()
        );

        assertEquals(new BigDecimal("100.00"), closure.getTotalPayments());
    }

    @Test
    void should_ThrowException_When_NegativeAmountProvided() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);

        assertThrows(IllegalArgumentException.class, () ->
                closure.applyTotals(new BigDecimal("-1"), 0, BigDecimal.ZERO, 0,
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, 0, Collections.emptyList()));
    }

    

    @Test
    void should_CloseClosure_When_StatusIsOpen() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);
        UUID closedBy = UUID.randomUUID();
        String closedByName = "Supervisor";

        closure.close(closedBy, closedByName, "Todo en orden");

        assertEquals(RegisterStatus.CLOSED, closure.getStatus());
        assertEquals(closedBy, closure.getClosedBy());
        assertEquals(closedByName, closure.getClosedByName());
        assertNotNull(closure.getClosedAt());
        assertEquals("Todo en orden", closure.getClosingNotes());
    }

    @Test
    void should_ThrowException_When_ClosingAlreadyClosedClosure() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);
        closure.close(UUID.randomUUID(), "Test", null);

        assertThrows(IllegalStateException.class, () ->
                closure.close(UUID.randomUUID(), "Another", null));
    }

    @Test
    void should_ThrowException_When_ClosingWithNullClosedBy() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);
        assertThrows(NullPointerException.class, () ->
                closure.close(null, "Test", null));
    }

    @Test
    void should_ThrowException_When_ClosingWithNullClosedByName() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);
        assertThrows(NullPointerException.class, () ->
                closure.close(UUID.randomUUID(), null, null));
    }

    @Test
    void should_ThrowException_When_ClosingWithBlankClosedByName() {
        CashRegister closure = CashRegister.open(CLOSURE_ID, CLOSURE_DATE, OPENED_BY, OPENED_BY_NAME);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                closure.close(UUID.randomUUID(), "   ", null));
        assertEquals("Closed by name must not be blank", exception.getMessage());
    }

    

    @Test
    void should_Reconstitute_When_AllFieldsProvided() {
        LocalDateTime now = LocalDateTime.now();
        UUID closedBy = UUID.randomUUID();
        List<CashRegisterDetail> details = List.of(
                CashRegisterDetail.create(UUID.randomUUID(), "Efectivo", 5, new BigDecimal("150.00"))
        );

        CashRegister closure = CashRegister.reconstitute(
                CLOSURE_ID, CLOSURE_DATE, RegisterStatus.CLOSED,
                OPENED_BY, OPENED_BY_NAME, now,
                closedBy, "Supervisor", now,
                new BigDecimal("2150.00"), new BigDecimal("500.00"),
                new BigDecimal("100.00"), new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("3500.00"),
                5, 1, 2, 3,
                new BigDecimal("2500.00"), new BigDecimal("2450.00"), new BigDecimal("50.00"),
                "Nota de cierre",
                1, now, now,
                details
        );

        assertEquals(CLOSURE_ID, closure.getId());
        assertEquals(RegisterStatus.CLOSED, closure.getStatus());
        assertEquals(new BigDecimal("2500.00"), closure.getExpectedClosingBalance());
        assertEquals(new BigDecimal("2450.00"), closure.getActualClosingBalance());
        assertEquals(new BigDecimal("50.00"), closure.getDifference());
        assertEquals("Nota de cierre", closure.getClosingNotes());
        assertEquals(1, closure.getVersion());
        assertEquals(1, closure.getDetails().size());
    }
}
