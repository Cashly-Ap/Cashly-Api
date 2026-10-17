package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.mappers;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashRegisterMapper Unit Tests")
class CashRegisterMapperTest {

    private static final UUID REGISTER_ID = UUID.randomUUID();
    private static final UUID OPENED_BY = UUID.randomUUID();
    private static final UUID CLOSED_BY = UUID.randomUUID();
    private static final UUID PAYMENT_METHOD_ID = UUID.randomUUID();

    private CashRegister createSampleRegister() {
        return CashRegister.reconstitute(
                RegisterId.of(REGISTER_ID),
                LocalDate.of(2026, 6, 15),
                RegisterStatus.CLOSED,
                OPENED_BY, "John Doe",
                LocalDateTime.of(2026, 6, 15, 8, 0),
                CLOSED_BY, "Jane Doe",
                LocalDateTime.of(2026, 6, 15, 18, 0),
                new BigDecimal("1500.00"), BigDecimal.ZERO,
                new BigDecimal("100.00"), new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("2000.00"),
                20, 0, 2, 15,
                new BigDecimal("1350.00"), null, null, "End of day",
                0,
                LocalDateTime.of(2026, 6, 15, 8, 0),
                LocalDateTime.of(2026, 6, 15, 18, 0),
                List.of(CashRegisterDetail.create(
                        PAYMENT_METHOD_ID, "Cash", 15, new BigDecimal("1500.00")
                ))
        );
    }

    @Nested
    @DisplayName("toResponse without DailyTotals")
    class WithoutDailyTotals {

        @Test
        void should_MapToResponse_When_RegisterProvided() {
            CashRegister register = createSampleRegister();

            CashRegisterResponse response = CashRegisterMapper.toResponse(register);

            assertNotNull(response);
            assertEquals(REGISTER_ID, response.id());
            assertEquals(RegisterStatus.CLOSED, response.status());
            assertEquals(OPENED_BY, response.openedBy());
            assertEquals("John Doe", response.openedByName());
            assertEquals(CLOSED_BY, response.closedBy());
            assertEquals("Jane Doe", response.closedByName());
            assertEquals("End of day", response.closingNotes());
        }

        @Test
        void should_UseRegisterTotals_When_DailyTotalsIsNull() {
            CashRegister register = createSampleRegister();

            CashRegisterResponse response = CashRegisterMapper.toResponse(register);

            assertEquals(new BigDecimal("2000.00"), response.totalInvoiced());
            assertEquals(new BigDecimal("1500.00"), response.totalRevenue());
            assertEquals(new BigDecimal("150.00"), response.totalExpenses());
            assertEquals(new BigDecimal("1350.00"), response.balance());
        }

        @Test
        void should_MapDetails_When_RegisterHasDetails() {
            CashRegister register = createSampleRegister();

            CashRegisterResponse response = CashRegisterMapper.toResponse(register);

            assertNotNull(response.details());
            assertEquals(1, response.details().size());
            assertEquals(PAYMENT_METHOD_ID, response.details().getFirst().paymentMethodId());
            assertEquals("Cash", response.details().getFirst().paymentMethodName());
            assertEquals(15, response.details().getFirst().transactionCount());
            assertEquals(new BigDecimal("1500.00"), response.details().getFirst().expectedAmount());
        }

        @Test
        void should_ReturnEmptyRevenueSummary_When_DailyTotalsIsNull() {
            CashRegister register = createSampleRegister();

            CashRegisterResponse response = CashRegisterMapper.toResponse(register);

            assertNotNull(response.revenueSummary());
            assertTrue(response.revenueSummary().isEmpty());
        }

        @Test
        void should_ReturnEmptyExpenseSummary_When_DailyTotalsIsNull() {
            CashRegister register = createSampleRegister();

            CashRegisterResponse response = CashRegisterMapper.toResponse(register);

            assertNotNull(response.expenseSummary());
            assertTrue(response.expenseSummary().isEmpty());
        }
    }

    @Nested
    @DisplayName("toResponse with DailyTotals")
    class WithDailyTotals {

        @Test
        void should_UseDailyTotals_When_Provided() {
            CashRegister register = createSampleRegister();
            DailyTotals dailyTotals = new DailyTotals(
                    new BigDecimal("1800.00"), 22,
                    BigDecimal.ZERO, 0,
                    new BigDecimal("80.00"), 1,
                    new BigDecimal("30.00"), BigDecimal.ZERO,
                    new BigDecimal("2500.00"), 18,
                    new BigDecimal("2500.00"),
                    new BigDecimal("1800.00"),
                    new BigDecimal("110.00"),
                    List.of(CashRegisterDetail.create(
                            PAYMENT_METHOD_ID, "Cash", 18, new BigDecimal("1800.00")
                    ))
            );

            CashRegisterResponse response = CashRegisterMapper.toResponse(register, dailyTotals);

            assertEquals(new BigDecimal("2500.00"), response.totalInvoiced());
            assertEquals(new BigDecimal("1800.00"), response.totalRevenue());
            assertEquals(new BigDecimal("110.00"), response.totalExpenses());
            assertEquals(new BigDecimal("1690.00"), response.balance());
        }

        @Test
        void should_BuildRevenueSummary_When_DailyTotalsHasPaymentDetails() {
            CashRegister register = createSampleRegister();
            DailyTotals dailyTotals = new DailyTotals(
                    new BigDecimal("1800.00"), 22,
                    BigDecimal.ZERO, 0,
                    new BigDecimal("80.00"), 1,
                    new BigDecimal("30.00"), BigDecimal.ZERO,
                    new BigDecimal("2500.00"), 18,
                    new BigDecimal("2500.00"),
                    new BigDecimal("1800.00"),
                    new BigDecimal("110.00"),
                    List.of(CashRegisterDetail.create(
                            PAYMENT_METHOD_ID, "Cash", 18, new BigDecimal("1800.00")
                    ))
            );

            CashRegisterResponse response = CashRegisterMapper.toResponse(register, dailyTotals);

            assertEquals(1, response.revenueSummary().size());
            assertEquals("Cash", response.revenueSummary().getFirst().paymentMethod());
            assertEquals(new BigDecimal("1800.00"), response.revenueSummary().getFirst().total());
        }

        @Test
        void should_BuildExpenseSummary_When_DailyTotalsHasReturnsAndCreditNotes() {
            CashRegister register = createSampleRegister();
            DailyTotals dailyTotals = new DailyTotals(
                    new BigDecimal("1800.00"), 22,
                    BigDecimal.ZERO, 0,
                    new BigDecimal("80.00"), 1,
                    new BigDecimal("30.00"), BigDecimal.ZERO,
                    new BigDecimal("2500.00"), 18,
                    new BigDecimal("2500.00"),
                    new BigDecimal("1800.00"),
                    new BigDecimal("110.00"),
                    null
            );

            CashRegisterResponse response = CashRegisterMapper.toResponse(register, dailyTotals);

            assertEquals(2, response.expenseSummary().size());
            assertEquals("Devoluciones", response.expenseSummary().get(0).concept());
            assertEquals(new BigDecimal("80.00"), response.expenseSummary().get(0).total());
            assertEquals("Notas de Crédito", response.expenseSummary().get(1).concept());
            assertEquals(new BigDecimal("30.00"), response.expenseSummary().get(1).total());
        }

        @Test
        void should_SkipExpenseSummary_When_NoReturnsOrCredits() {
            CashRegister register = createSampleRegister();
            DailyTotals dailyTotals = new DailyTotals(
                    new BigDecimal("1800.00"), 22,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("2500.00"), 18,
                    new BigDecimal("2500.00"),
                    new BigDecimal("1800.00"),
                    BigDecimal.ZERO,
                    List.of()
            );

            CashRegisterResponse response = CashRegisterMapper.toResponse(register, dailyTotals);

            assertTrue(response.expenseSummary().isEmpty());
        }
    }

    @Nested
    @DisplayName("toResponse without details")
    class WithoutDetails {

        @Test
        void should_HandleEmptyDetailsList() {
            CashRegister register = CashRegister.reconstitute(
                    RegisterId.of(REGISTER_ID),
                    LocalDate.of(2026, 6, 15),
                    RegisterStatus.OPEN,
                    OPENED_BY, "John Doe",
                    LocalDateTime.of(2026, 6, 15, 8, 0),
                    null, null, null,
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0, 0, 0, 0,
                    BigDecimal.ZERO, null, null, null,
                    0,
                    LocalDateTime.of(2026, 6, 15, 8, 0),
                    LocalDateTime.of(2026, 6, 15, 8, 0),
                    List.of()
            );

            CashRegisterResponse response = CashRegisterMapper.toResponse(register);

            assertNotNull(response.details());
            assertTrue(response.details().isEmpty());
        }
    }
}
