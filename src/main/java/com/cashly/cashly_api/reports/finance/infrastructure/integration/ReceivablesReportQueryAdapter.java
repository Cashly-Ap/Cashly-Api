package com.cashly.cashly_api.reports.finance.infrastructure.integration;
import com.cashly.cashly_api.receivables.application.api.AccountsReceivableReportQuery;
import com.cashly.cashly_api.reports.finance.application.contracts.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.reports.finance.application.dto.AccountsReceivableReportData.AccountReceivableRow;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
@Component
public class ReceivablesReportQueryAdapter implements AccountsReceivableQueryGateway {
    private final AccountsReceivableReportQuery query;
    public ReceivablesReportQueryAdapter(AccountsReceivableReportQuery query) { this.query = query; }
    public List<AccountReceivableRow> findAccountsReceivableForReport(UUID customerId, boolean overdueOnly) {
        return query.find(customerId, overdueOnly).stream().map(row ->
                new AccountReceivableRow(row.customerId(), row.customerName(), row.rif(), row.creditLimit(), row.pendingBalance(), row.availableCredit(), row.age0_30(), row.age31_60(), row.age61_90(), row.age91_120(), row.age120Plus())).toList();
    }
}