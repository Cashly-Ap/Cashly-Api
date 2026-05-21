package com.parsun.parsun_api.reports.finances.application.usecases;

import com.parsun.parsun_api.reports.finances.application.dto.AccountsReceivableReportData;

import java.util.UUID;

public interface GenerateAccountsReceivableReportPdfUseCase {
    byte[] execute(UUID customerId, boolean overdueOnly);
}
