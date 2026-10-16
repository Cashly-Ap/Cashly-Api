package com.cashly.cashly_api.cash_register.sessions.application.dto;

import com.cashly.cashly_api.cash_register.sessions.domain.CashRegisterDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DailyTotals Unit Tests")
class DailyTotalsTest {

    private final CashRegisterDetail detail = CashRegisterDetail.create(
            UUID.randomUUID(), "Cash", 15, new BigDecimal("1500.00")
    );

    @Nested
    @DisplayName("Record creation")
    class Creation {

        @Test
        void should_CreateWithAllFields() {
            DailyTotals totals = new DailyTotals(
                    new BigDecimal("1500.00"), 20,
                    new BigDecimal("200.00"), 2,
                    new BigDecimal("50.00"), 1,
                    new BigDecimal("30.00"), BigDecimal.TEN,
                    new BigDecimal("2000.00"), 15,
                    new BigDecimal("2000.00"),
                    new BigDecimal("1500.00"),
                    new BigDecimal("80.00"),
                    List.of(detail)
            );

            assertEquals(new BigDecimal("1500.00"), totals.totalPayments());
            assertEquals(20, totals.paymentCount());
            assertEquals(new BigDecimal("200.00"), totals.totalAdvances());
            assertEquals(2, totals.advanceCount());
            assertEquals(new BigDecimal("50.00"), totals.totalReturns());
            assertEquals(new BigDecimal("30.00"), totals.totalCreditNotes());
            assertEquals(new BigDecimal("2000.00"), totals.totalInvoiced());
            assertEquals(new BigDecimal("1500.00"), totals.totalRevenue());
            assertEquals(new BigDecimal("80.00"), totals.totalExpenses());
            assertEquals(1, totals.paymentDetails().size());
        }
    }

    @Nested
    @DisplayName("Default values")
    class DefaultValues {

        @Test
        void should_DefaultTotalInvoiced_When_Null() {
            DailyTotals totals = new DailyTotals(
                    new BigDecimal("1500.00"), 20,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("2000.00"), 15,
                    null,
                    new BigDecimal("1500.00"),
                    BigDecimal.ZERO,
                    List.of()
            );

            assertEquals(new BigDecimal("2000.00"), totals.totalInvoiced());
        }

        @Test
        void should_DefaultTotalRevenue_When_Null() {
            DailyTotals totals = new DailyTotals(
                    new BigDecimal("1500.00"), 20,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("2000.00"), 15,
                    new BigDecimal("2000.00"),
                    null,
                    BigDecimal.ZERO,
                    List.of()
            );

            assertEquals(new BigDecimal("1500.00"), totals.totalRevenue());
        }

        @Test
        void should_DefaultTotalExpenses_When_Null() {
            DailyTotals totals = new DailyTotals(
                    new BigDecimal("1500.00"), 20,
                    BigDecimal.ZERO, 0,
                    new BigDecimal("50.00"), 1,
                    new BigDecimal("30.00"), BigDecimal.ZERO,
                    new BigDecimal("2000.00"), 15,
                    new BigDecimal("2000.00"),
                    new BigDecimal("1500.00"),
                    null,
                    List.of()
            );

            assertEquals(new BigDecimal("80.00"), totals.totalExpenses());
        }

        @Test
        void should_DefaultToZeroExpenses_When_ReturnsAndCreditsAreZero() {
            DailyTotals totals = new DailyTotals(
                    new BigDecimal("1500.00"), 20,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("2000.00"), 15,
                    new BigDecimal("2000.00"),
                    new BigDecimal("1500.00"),
                    null,
                    List.of()
            );

            assertEquals(BigDecimal.ZERO, totals.totalExpenses());
        }
    }

    @Nested
    @DisplayName("Record immutability")
    class Immutability {

        @Test
        void should_ImplementEqualsAndHashCode() {
            DailyTotals t1 = new DailyTotals(
                    BigDecimal.TEN, 1, BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.TEN, 1,
                    BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO,
                    List.of()
            );
            DailyTotals t2 = new DailyTotals(
                    BigDecimal.TEN, 1, BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.TEN, 1,
                    BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO,
                    List.of()
            );

            assertEquals(t1, t2);
            assertEquals(t1.hashCode(), t2.hashCode());
        }
    }
}
