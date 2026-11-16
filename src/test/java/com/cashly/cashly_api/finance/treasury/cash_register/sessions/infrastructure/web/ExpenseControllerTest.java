package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model.ExpenseFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response.ExpenseResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.use_case.ListExpensesUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpenseControllerTest {

    private final ListExpensesUseCase useCase = mock(ListExpensesUseCase.class);
    private final ExpenseController controller = new ExpenseController(useCase);

    @Test
    void shouldPassFiltersToUseCase() {
        var registerId = UUID.randomUUID();
        var paymentMethodId = UUID.randomUUID();
        var expected = new PageResponse<ExpenseResponse>(List.of(), 0, 0, 20,
                0, true, true, true);
        when(useCase.execute(any(ExpenseFilter.class))).thenReturn(expected);

        var result = controller.list(0, 20, "amount,asc",
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 11),
                registerId, paymentMethodId, "limpieza");

        assertThat(result).isSameAs(expected);
        verify(useCase).execute(new ExpenseFilter(0, 20, "amount,asc",
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 11),
                registerId, paymentMethodId, "limpieza"));
    }
}
