package com.parsun.parsun_api.reports.finances.application.port;

import com.parsun.parsun_api.reports.finances.application.dto.AccountsReceivableReportData;

import java.util.UUID;

public interface AccountsReceivableReportPdfPort {
    byte[] generateReport(AccountsReceivableReportData data);
}
