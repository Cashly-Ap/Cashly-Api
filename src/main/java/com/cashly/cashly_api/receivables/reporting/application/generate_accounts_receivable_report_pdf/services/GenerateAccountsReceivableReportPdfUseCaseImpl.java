package com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.services;

import com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.use_case.GenerateAccountsReceivableReportPdfUseCase;

import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData;
import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData.AccountReceivableRow;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class GenerateAccountsReceivableReportPdfUseCaseImpl implements GenerateAccountsReceivableReportPdfUseCase {

    private final AccountsReceivableQueryGateway receivablesRepository;
    private final AccountsReceivableReportPdfPort pdfPort;

    public GenerateAccountsReceivableReportPdfUseCaseImpl(
            AccountsReceivableQueryGateway receivablesRepository,
            AccountsReceivableReportPdfPort pdfPort) {
        this.receivablesRepository = receivablesRepository;
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(UUID customerId, boolean overdueOnly) {
        AccountsReceivableReportData data = buildReportData(customerId, overdueOnly);
        return pdfPort.generateReport(data);
    }

    private AccountsReceivableReportData buildReportData(UUID customerId, boolean overdueOnly) {
        List<AccountReceivableRow> reportRows = receivablesRepository.findAccountsReceivableForReport(customerId, overdueOnly);

        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal notYetDue = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;
        int customersExceededCredit = 0;

        for (AccountReceivableRow row : reportRows) {
            BigDecimal rowBalance = row.pendingBalance();
            totalReceivable = totalReceivable.add(rowBalance);

            BigDecimal rowAge0_30 = row.age0_30();
            notYetDue = notYetDue.add(rowAge0_30);

            BigDecimal rowOverdue = row.age31_60()
                    .add(row.age61_90())
                    .add(row.age91_120())
                    .add(row.age120Plus());
            overdue = overdue.add(rowOverdue);

            if (row.pendingBalance().compareTo(row.creditLimit()) > 0) {
                customersExceededCredit++;
            }
        }

        return new AccountsReceivableReportData(
                java.time.LocalDate.now(),
                overdueOnly,
                reportRows,
                new AccountsReceivableReportData.Summary(
                        totalReceivable,
                        notYetDue,
                        overdue,
                        customersExceededCredit
                )
        );
    }
}
