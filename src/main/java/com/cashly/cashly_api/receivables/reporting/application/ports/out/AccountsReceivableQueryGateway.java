package com.cashly.cashly_api.receivables.reporting.application.ports.out;
import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData.AccountReceivableRow;
import java.util.List;
import java.util.UUID;
public interface AccountsReceivableQueryGateway {
    List<AccountReceivableRow> findAccountsReceivableForReport(UUID customerId, boolean overdueOnly);
}