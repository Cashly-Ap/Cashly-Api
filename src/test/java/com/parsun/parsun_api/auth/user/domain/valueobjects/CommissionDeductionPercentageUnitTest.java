package com.parsun.parsun_api.auth.user.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommissionDeductionPercentage Unit Tests")
class CommissionDeductionPercentageUnitTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("should create with valid value")
        void should_Create_When_ValidValueProvided() {
            CommissionDeductionPercentage vo = new CommissionDeductionPercentage(new BigDecimal("1.00"));

            assertThat(vo.value()).isEqualByComparingTo("1.00");
        }

        @Test
        @DisplayName("should create with zero")
        void should_Create_When_ZeroProvided() {
            CommissionDeductionPercentage vo = new CommissionDeductionPercentage(BigDecimal.ZERO);

            assertThat(vo.value()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("should create with max value 100")
        void should_Create_When_MaxValueProvided() {
            CommissionDeductionPercentage vo = new CommissionDeductionPercentage(new BigDecimal("100"));

            assertThat(vo.value()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("should scale to 2 decimal places")
        void should_ScaleToTwoDecimals_When_Created() {
            CommissionDeductionPercentage vo = new CommissionDeductionPercentage(new BigDecimal("1.5"));

            assertThat(vo.value().scale()).isEqualTo(2);
            assertThat(vo.value()).isEqualByComparingTo("1.50");
        }

        @Test
        @DisplayName("should throw when null")
        void should_Throw_When_NullProvided() {
            assertThatThrownBy(() -> new CommissionDeductionPercentage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Commission deduction percentage cannot be null");
        }

        @Test
        @DisplayName("should throw when negative")
        void should_Throw_When_NegativeProvided() {
            assertThatThrownBy(() -> new CommissionDeductionPercentage(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Commission deduction percentage cannot be negative");
        }

        @Test
        @DisplayName("should throw when exceeds 100")
        void should_Throw_When_ExceedsHundred() {
            assertThatThrownBy(() -> new CommissionDeductionPercentage(new BigDecimal("100.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Commission deduction percentage cannot exceed 100%");
        }
    }

    @Nested
    @DisplayName("Factory Tests")
    class FactoryTests {

        @Test
        @DisplayName("should create with of() factory method")
        void should_Create_When_OfFactoryUsed() {
            CommissionDeductionPercentage vo = CommissionDeductionPercentage.of(new BigDecimal("2.50"));

            assertThat(vo.value()).isEqualByComparingTo("2.50");
        }

        @Test
        @DisplayName("should create default one percent")
        void should_CreateOnePercent_When_DefaultUsed() {
            CommissionDeductionPercentage vo = CommissionDeductionPercentage.defaultValue();

            assertThat(vo.value()).isEqualByComparingTo("1.00");
        }
    }

    @Nested
    @DisplayName("Calculation Tests")
    class CalculationTests {

        @Test
        @DisplayName("should deduct correctly for given months")
        void should_DeductCorrectly_When_MonthsProvided() {
            CommissionDeductionPercentage deduction = new CommissionDeductionPercentage(new BigDecimal("1.00"));
            BigDecimal nominalCommission = new BigDecimal("5.00");

            BigDecimal effective = deduction.applyDeduction(nominalCommission, 2);

            assertThat(effective).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("should not go below zero when deduction exceeds commission")
        void should_ReturnZero_When_DeductionExceedsCommission() {
            CommissionDeductionPercentage deduction = new CommissionDeductionPercentage(new BigDecimal("3.00"));
            BigDecimal nominalCommission = new BigDecimal("5.00");

            BigDecimal effective = deduction.applyDeduction(nominalCommission, 3);

            assertThat(effective).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("should return same commission when zero months delay")
        void should_ReturnUnchanged_When_ZeroMonths() {
            CommissionDeductionPercentage deduction = new CommissionDeductionPercentage(new BigDecimal("1.00"));
            BigDecimal nominalCommission = new BigDecimal("5.00");

            BigDecimal effective = deduction.applyDeduction(nominalCommission, 0);

            assertThat(effective).isEqualByComparingTo("5.00");
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("should be equal when same value")
        void should_BeEqual_When_SameValue() {
            CommissionDeductionPercentage a = new CommissionDeductionPercentage(new BigDecimal("1.00"));
            CommissionDeductionPercentage b = new CommissionDeductionPercentage(new BigDecimal("1.00"));

            assertThat(a).isEqualTo(b);
        }

        @Test
        @DisplayName("should not be equal when different value")
        void should_NotBeEqual_When_DifferentValue() {
            CommissionDeductionPercentage a = new CommissionDeductionPercentage(new BigDecimal("1.00"));
            CommissionDeductionPercentage b = new CommissionDeductionPercentage(new BigDecimal("2.00"));

            assertThat(a).isNotEqualTo(b);
        }
    }
}
