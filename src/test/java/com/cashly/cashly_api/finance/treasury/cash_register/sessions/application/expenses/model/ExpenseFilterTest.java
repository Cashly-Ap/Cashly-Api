package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpenseFilterTest {

    @Test
    void shouldUseDefaultSortWhenBlank() {
        var filter = new ExpenseFilter(0, 10, " ", null, null, null, null,
                null);

        assertThat(filter.sort()).isEqualTo("occurredAt,desc");
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThatThrownBy(
                () -> new ExpenseFilter(-1, 10, null, null, null, null, null,
                        null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                () -> new ExpenseFilter(0, 0, null, null, null, null, null,
                        null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidDateRange() {
        assertThatThrownBy(() -> new ExpenseFilter(0, 10, null,
                LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 3), null,
                null, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
