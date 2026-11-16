package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model.ExpenseFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response.ExpenseResponse;
import com.cashly.cashly_api.shared.dto.PageResponse;

public interface ExpenseQueryGateway {
    PageResponse<ExpenseResponse> findExpenses(ExpenseFilter filter);
}
