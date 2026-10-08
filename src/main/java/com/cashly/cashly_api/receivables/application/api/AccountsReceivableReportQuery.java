package com.cashly.cashly_api.receivables.application.api;
import java.util.List;
import java.util.UUID;
public interface AccountsReceivableReportQuery {
    List<AccountsReceivableReportSnapshot> find(UUID customerId, boolean overdueOnly);
}