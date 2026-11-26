package com.cashly.cashly_api.finance.treasury.receivables.application.ports.in;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public interface AccountsReceivableReportQuery {
    List<AccountsReceivableReportSnapshot> find(UUID customerId,
            boolean overdueOnly, LocalDate dateFrom, LocalDate dateTo);
}