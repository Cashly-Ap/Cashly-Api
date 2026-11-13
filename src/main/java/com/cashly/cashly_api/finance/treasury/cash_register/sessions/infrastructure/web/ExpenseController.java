package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.model.ExpenseFilter;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.response.ExpenseResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.expenses.use_case.ListExpensesUseCase;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/cash-register/expenses")
public class ExpenseController {

    private final ListExpensesUseCase listExpensesUseCase;

    public ExpenseController(ListExpensesUseCase listExpensesUseCase) {
        this.listExpensesUseCase = listExpensesUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FINANCE_TREASURY_EXPENSES_VIEW_LIST','FINANCE_TREASURY_CASH_REGISTER_VIEW_LIST')")
    public PageResponse<ExpenseResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) UUID registerId,
            @RequestParam(required = false) UUID paymentMethodId,
            @RequestParam(required = false) String search) {
        return listExpensesUseCase.execute(new ExpenseFilter(page, size, sort,
                dateFrom, dateTo, registerId, paymentMethodId, search));
    }
}
