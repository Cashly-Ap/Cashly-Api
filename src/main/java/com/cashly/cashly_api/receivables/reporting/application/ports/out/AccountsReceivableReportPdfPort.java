package com.cashly.cashly_api.receivables.reporting.application.ports.out;

import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData;

import java.util.UUID;

public interface AccountsReceivableReportPdfPort {
    byte[] generateReport(AccountsReceivableReportData data);
}
