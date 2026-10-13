package com.cashly.cashly_api.reports.invoices.infrastructure.config;

import com.cashly.cashly_api.reports.invoices.application.ports.out.InvoicePdfPort;
import com.cashly.cashly_api.reports.invoices.application.ports.out.InvoicesReportPdfPort;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCaseImpl;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.invoices.infrastructure.adapters.out.pdf.InvoicePdfGenerator;
import com.cashly.cashly_api.reports.invoices.infrastructure.adapters.out.pdf.InvoicesReportPdfGenerator;
import com.cashly.cashly_api.reports.invoices.application.ports.out.InvoiceReportQueryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoicesReportConfig {

    @Bean
    public InvoicePdfPort invoicePdfPort() {
        return new InvoicePdfGenerator();
    }

    @Bean
    public InvoicesReportPdfPort invoicesReportPdfPort() {
        return new InvoicesReportPdfGenerator();
    }

    @Bean
    public GenerateInvoicePdfUseCase generateInvoicePdfUseCase(
            InvoiceReportQueryGateway query,
            InvoicePdfPort invoicePdfPort) {
        return new GenerateInvoicePdfUseCaseImpl(
                query,
                invoicePdfPort
        );
    }

    @Bean
    public GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase(
            InvoiceReportQueryGateway query, InvoicesReportPdfPort invoicesReportPdfPort) {
        return new GenerateInvoicesReportPdfUseCaseImpl(query, invoicesReportPdfPort);
    }
}
