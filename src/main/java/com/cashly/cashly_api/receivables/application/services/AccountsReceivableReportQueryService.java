package com.cashly.cashly_api.receivables.application.services;
import com.cashly.cashly_api.receivables.application.api.AccountsReceivableReportQuery;
import com.cashly.cashly_api.receivables.application.api.AccountsReceivableReportSnapshot;
import com.cashly.cashly_api.receivables.application.contracts.ReceivablesQueryGateway;
import java.util.List;
import java.util.UUID;
public class AccountsReceivableReportQueryService implements AccountsReceivableReportQuery {
    private final ReceivablesQueryGateway query;
    public AccountsReceivableReportQueryService(ReceivablesQueryGateway query) { this.query = query; }
    public List<AccountsReceivableReportSnapshot> find(UUID customerId, boolean overdueOnly) {
        return query.findAccountsReceivableForReport(customerId, overdueOnly).stream().map(row ->
                new AccountsReceivableReportSnapshot(row.customerId(), row.customerName(), row.rif(), row.creditLimit(), row.pendingBalance(), row.availableCredit(), row.age0_30(), row.age31_60(), row.age61_90(), row.age91_120(), row.age120Plus())).toList();
    }
}