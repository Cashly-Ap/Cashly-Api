package com.parsun.parsun_api.reports.finance.application.port;

import com.parsun.parsun_api.reports.finance.application.dto.AccountsReceivableReportData;

import java.util.UUID;

public interface AccountsReceivableReportPdfPort {
    byte[] generateReport(AccountsReceivableReportData data);
}
