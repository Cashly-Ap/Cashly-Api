package com.parsun.parsun_api.cashregister.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashRegisterMetrics Unit Tests")
class CashRegisterMetricsTest {

    @Nested
    @DisplayName("Record creation")
    class Creation {

        @Test
        void should_CreateWithAllFields() {
            List<RevenueSummaryItem> revenue = List.of(
                    new RevenueSummaryItem("Cash", new BigDecimal("1000.00"))
            );
            List<ExpenseSummaryItem> expenses = List.of(
                    new ExpenseSummaryItem("Devoluciones", new BigDecimal("50.00"))
            );

            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    new BigDecimal("2000.00"),
                    new BigDecimal("1000.00"),
                    new BigDecimal("50.00"),
                    new BigDecimal("950.00"),
                    revenue, expenses
            );

            assertEquals(new BigDecimal("2000.00"), metrics.totalInvoiced());
            assertEquals(new BigDecimal("1000.00"), metrics.totalRevenue());
            assertEquals(new BigDecimal("50.00"), metrics.totalExpenses());
            assertEquals(new BigDecimal("950.00"), metrics.balance());
            assertEquals(1, metrics.revenueSummary().size());
            assertEquals(1, metrics.expenseSummary().size());
        }
    }

    @Nested
    @DisplayName("Balance auto-calculation")
    class BalanceCalculation {

        @Test
        void should_CalculateBalance_When_NullProvided() {
            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    new BigDecimal("2000.00"),
                    new BigDecimal("1000.00"),
                    new BigDecimal("100.00"),
                    null,
                    List.of(), List.of()
            );

            assertEquals(new BigDecimal("900.00"), metrics.balance());
        }

        @Test
        void should_RoundBalanceToTwoDecimals() {
            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    new BigDecimal("2000.00"),
                    new BigDecimal("100.00"),
                    new BigDecimal("33.33"),
                    null,
                    List.of(), List.of()
            );

            BigDecimal expected = new BigDecimal("66.67").setScale(2, RoundingMode.HALF_UP);
            assertEquals(expected, metrics.balance());
        }

        @Test
        void should_ReturnZeroBalance_When_RevenueAndExpensesAreNull() {
            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    new BigDecimal("2000.00"),
                    null,
                    null,
                    null,
                    List.of(), List.of()
            );

            assertEquals(BigDecimal.ZERO, metrics.balance());
        }

        @Test
        void should_PreserveExplicitBalance_When_Provided() {
            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    new BigDecimal("2000.00"),
                    new BigDecimal("1000.00"),
                    new BigDecimal("100.00"),
                    new BigDecimal("500.00"),
                    List.of(), List.of()
            );

            assertEquals(new BigDecimal("500.00"), metrics.balance());
        }
    }

    @Nested
    @DisplayName("Empty summaries")
    class EmptySummaries {

        @Test
        void should_AcceptEmptyRevenueSummary() {
            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of()
            );

            assertTrue(metrics.revenueSummary().isEmpty());
        }

        @Test
        void should_AcceptEmptyExpenseSummary() {
            CashRegisterMetrics metrics = new CashRegisterMetrics(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of()
            );

            assertTrue(metrics.expenseSummary().isEmpty());
        }
    }

    @Nested
    @DisplayName("Record immutability")
    class Immutability {

        @Test
        void should_BeRecord() {
            assertTrue(CashRegisterMetrics.class.isRecord());
        }

        @Test
        void should_ImplementEqualsAndHashCode() {
            CashRegisterMetrics m1 = new CashRegisterMetrics(
                    BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                    List.of(), List.of()
            );
            CashRegisterMetrics m2 = new CashRegisterMetrics(
                    BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE,
                    List.of(), List.of()
            );

            assertEquals(m1, m2);
            assertEquals(m1.hashCode(), m2.hashCode());
        }
    }
}
