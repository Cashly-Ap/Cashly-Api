package com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.use_case;

import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData;

import java.util.UUID;

public interface GenerateAccountsReceivableReportPdfUseCase {
    byte[] execute(UUID customerId, boolean overdueOnly);
}
