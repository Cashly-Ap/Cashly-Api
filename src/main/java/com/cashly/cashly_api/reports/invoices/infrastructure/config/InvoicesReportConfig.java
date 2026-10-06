package com.cashly.cashly_api.reports.invoices.infrastructure.config;

import com.cashly.cashly_api.reports.invoices.application.port.InvoicePdfPort;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicesReportPdfPort;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCaseImpl;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.invoices.infrastructure.pdf.InvoicePdfGenerator;
import com.cashly.cashly_api.reports.invoices.infrastructure.pdf.InvoicesReportPdfGenerator;
import com.cashly.cashly_api.invoices.application.usecases.GetInvoiceByIdUseCase;
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
            GetInvoiceByIdUseCase getInvoiceByIdUseCase,
            InvoicePdfPort invoicePdfPort) {
        return new GenerateInvoicePdfUseCaseImpl(
                getInvoiceByIdUseCase,
                invoicePdfPort
        );
    }

    @Bean
    public GenerateInvoicesReportPdfUseCase generateInvoicesReportPdfUseCase(
            InvoicesReportPdfPort invoicesReportPdfPort) {
        return new GenerateInvoicesReportPdfUseCaseImpl(invoicesReportPdfPort);
    }
}
