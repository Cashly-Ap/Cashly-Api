package com.cashly.cashly_api.reports.finance.application.usecases;

import com.cashly.cashly_api.reports.finance.application.dto.AccountsReceivableReportData;

import java.util.UUID;

public interface GenerateAccountsReceivableReportPdfUseCase {
    byte[] execute(UUID customerId, boolean overdueOnly);
}
