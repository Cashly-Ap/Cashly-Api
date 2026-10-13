package com.cashly.cashly_api.reports.finance.application.ports.out;

import com.cashly.cashly_api.reports.finance.application.dto.AccountsReceivableReportData;

import java.util.UUID;

public interface AccountsReceivableReportPdfPort {
    byte[] generateReport(AccountsReceivableReportData data);
}
