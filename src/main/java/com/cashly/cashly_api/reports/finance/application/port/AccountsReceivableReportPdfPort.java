package com.cashly.cashly_api.reports.finance.application.port;

import com.cashly.cashly_api.reports.finance.application.dto.AccountsReceivableReportData;

import java.util.UUID;

public interface AccountsReceivableReportPdfPort {
    byte[] generateReport(AccountsReceivableReportData data);
}
