package com.cashly.cashly_api.receivables.reporting.infrastructure.config;

import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.CollectionsReportPdfPort;
import com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.GenerateAccountsReceivableReportPdfUseCase;
import com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.GenerateAccountsReceivableReportPdfUseCaseImpl;
import com.cashly.cashly_api.receivables.reporting.application.generate_collections_report_pdf.GenerateCollectionsReportPdfUseCase;
import com.cashly.cashly_api.receivables.reporting.application.generate_collections_report_pdf.GenerateCollectionsReportPdfUseCaseImpl;
import com.cashly.cashly_api.receivables.reporting.infrastructure.pdf.AccountsReceivableReportPdfGenerator;
import com.cashly.cashly_api.receivables.reporting.infrastructure.pdf.CollectionsReportPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReceivablesReportingConfig {

    @Bean
    public AccountsReceivableReportPdfPort accountsReceivableReportPdfPort() {
        return new AccountsReceivableReportPdfGenerator();
    }

    @Bean
    public CollectionsReportPdfPort collectionsReportPdfPort() {
        return new CollectionsReportPdfGenerator();
    }

    @Bean
    public GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase(
            AccountsReceivableQueryGateway receivablesRepository,
            AccountsReceivableReportPdfPort pdfPort) {
        return new GenerateAccountsReceivableReportPdfUseCaseImpl(receivablesRepository, pdfPort);
    }

    @Bean
    public GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase(
            CollectionsReportPdfPort pdfPort) {
        return new GenerateCollectionsReportPdfUseCaseImpl(pdfPort);
    }
}
