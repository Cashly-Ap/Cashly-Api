package com.parsun.parsun_api.reports.finance.application.usecases;

import com.parsun.parsun_api.reports.finance.application.dto.AccountsReceivableReportData;
import com.parsun.parsun_api.reports.finance.application.port.AccountsReceivableReportPdfPort;

import java.util.UUID;

public class GenerateAccountsReceivableReportPdfUseCaseImpl implements GenerateAccountsReceivableReportPdfUseCase {

    private final AccountsReceivableReportPdfPort pdfPort;

    public GenerateAccountsReceivableReportPdfUseCaseImpl(AccountsReceivableReportPdfPort pdfPort) {
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(UUID customerId, boolean overdueOnly) {
        AccountsReceivableReportData data = buildReportData(customerId, overdueOnly);
        return pdfPort.generateReport(data);
    }

    private AccountsReceivableReportData buildReportData(UUID customerId, boolean overdueOnly) {
        return new AccountsReceivableReportData(
                java.time.LocalDate.now(),
                overdueOnly,
                java.util.List.of(),
                new AccountsReceivableReportData.Summary(
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO,
                        java.math.BigDecimal.ZERO,
                        0
                )
        );
    }
}
