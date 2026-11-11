package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model.ExpenseFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response.ExpenseResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.ExpenseQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListExpensesUseCaseTest {

    private final ExpenseQueryGateway gateway = mock(ExpenseQueryGateway.class);
    private final ListExpensesUseCase useCase = new ListExpensesUseCase(gateway);

    @Test
    void shouldDelegateFiltersToGateway() {
        var filter = new ExpenseFilter(0, 10, "occurredAt,desc",
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 11),
                UUID.randomUUID(), UUID.randomUUID(), "limpieza");
        var response = new PageResponse<ExpenseResponse>(List.of(), 0, 0, 10, 0,
                true, true, true);
        when(gateway.findExpenses(filter)).thenReturn(response);

        var result = useCase.execute(filter);

        assertThat(result).isSameAs(response);
        verify(gateway).findExpenses(filter);
    }
}
