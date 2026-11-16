package com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.ports.out.CollectionsReportPdfPort;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_accounts_receivable_report_pdf.use_case.GenerateAccountsReceivableReportPdfUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_accounts_receivable_report_pdf.services.GenerateAccountsReceivableReportPdfUseCaseImpl;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_collections_report_pdf.use_case.GenerateCollectionsReportPdfUseCase;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.generate_collections_report_pdf.services.GenerateCollectionsReportPdfUseCaseImpl;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.pdf.AccountsReceivableReportPdfGenerator;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.pdf.CollectionsReportPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceivablesReportingConfig {

    @Bean
    public AccountsReceivableReportPdfPort accountsReceivableReportPdfPort(
            PdfBranding branding) {
        return new AccountsReceivableReportPdfGenerator(branding);
    }

    @Bean
    public CollectionsReportPdfPort collectionsReportPdfPort(
            PdfBranding branding) {
        return new CollectionsReportPdfGenerator(branding);
    }

    @Bean
    public GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase(
            AccountsReceivableQueryGateway receivablesRepository,
            AccountsReceivableReportPdfPort pdfPort) {
        return new GenerateAccountsReceivableReportPdfUseCaseImpl(
                receivablesRepository, pdfPort);
    }

    @Bean
    public GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase(
            CollectionsReportPdfPort pdfPort) {
        return new GenerateCollectionsReportPdfUseCaseImpl(pdfPort);
    }
}
