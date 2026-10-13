package com.cashly.cashly_api.receivables.application.ports.in;
import java.util.List;
import java.util.UUID;
public interface AccountsReceivableReportQuery {
    List<AccountsReceivableReportSnapshot> find(UUID customerId, boolean overdueOnly);
}