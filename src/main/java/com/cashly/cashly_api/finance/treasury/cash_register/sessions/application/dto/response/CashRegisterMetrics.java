package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record CashRegisterMetrics(BigDecimal totalInvoiced,
        BigDecimal totalRevenue, BigDecimal totalExpenses, BigDecimal balance,
        List<RevenueSummaryItem> revenueSummary,
        List<ExpenseSummaryItem> expenseSummary) {
    public CashRegisterMetrics {
        if (balance == null) {
            balance = totalRevenue != null && totalExpenses != null
                    ? totalRevenue.subtract(totalExpenses).setScale(2,
                            RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }
    }
}
