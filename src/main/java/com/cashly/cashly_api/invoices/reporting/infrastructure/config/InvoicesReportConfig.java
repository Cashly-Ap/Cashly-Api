package com.cashly.cashly_api.invoices.reporting.infrastructure.config;

import com.cashly.cashly_api.invoices.reporting.application.ports.out.InvoicePdfPort;
import com.cashly.cashly_api.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import com.cashly.cashly_api.invoices.reporting.application.generate_invoice_pdf.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.invoices.reporting.application.generate_invoice_pdf.GenerateInvoicePdfUseCaseImpl;
import com.cashly.cashly_api.invoices.reporting.application.generate_invoices_report_pdf.GenerateInvoicesReportPdfUseCase;
import com.cashly.cashly_api.invoices.reporting.application.generate_invoices_report_pdf.GenerateInvoicesReportPdfUseCaseImpl;
import com.cashly.cashly_api.invoices.reporting.infrastructure.pdf.InvoicePdfGenerator;
import com.cashly.cashly_api.invoices.reporting.infrastructure.pdf.InvoicesReportPdfGenerator;
import com.cashly.cashly_api.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
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
