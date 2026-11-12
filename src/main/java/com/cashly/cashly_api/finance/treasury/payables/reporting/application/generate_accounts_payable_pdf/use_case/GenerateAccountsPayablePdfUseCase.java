package com.cashly.cashly_api.finance.treasury.payables.reporting.application.generate_accounts_payable_pdf.use_case;

import com.cashly.cashly_api.finance.treasury.payables.reporting.application.ports.out.AccountsPayableReportPdfPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class GenerateAccountsPayablePdfUseCase {

    private final AccountsPayableReportPdfPort pdfPort;

    public GenerateAccountsPayablePdfUseCase(
            AccountsPayableReportPdfPort pdfPort) {
        this.pdfPort = pdfPort;
    }

    @Transactional(readOnly = true)
    public byte[] execute(UUID supplierId, boolean overdueOnly) {
        return pdfPort.generate(supplierId, overdueOnly);
    }
}
