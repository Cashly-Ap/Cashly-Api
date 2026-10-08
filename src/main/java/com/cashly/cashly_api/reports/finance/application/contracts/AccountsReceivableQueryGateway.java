package com.cashly.cashly_api.reports.finance.application.contracts;
import com.cashly.cashly_api.reports.finance.application.dto.AccountsReceivableReportData.AccountReceivableRow;
import java.util.List;
import java.util.UUID;
public interface AccountsReceivableQueryGateway {
    List<AccountReceivableRow> findAccountsReceivableForReport(UUID customerId, boolean overdueOnly);
}