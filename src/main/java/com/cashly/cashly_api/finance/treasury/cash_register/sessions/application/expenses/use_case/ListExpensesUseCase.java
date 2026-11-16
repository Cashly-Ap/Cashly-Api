package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.use_case;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model.ExpenseFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response.ExpenseResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.ExpenseQueryGateway;
import com.cashly.cashly_api.shared.dto.PageResponse;

public class ListExpensesUseCase {

    private final ExpenseQueryGateway gateway;

    public ListExpensesUseCase(ExpenseQueryGateway gateway) {
        this.gateway = gateway;
    }

    public PageResponse<ExpenseResponse> execute(ExpenseFilter filter) {
        return gateway.findExpenses(filter);
    }
}
