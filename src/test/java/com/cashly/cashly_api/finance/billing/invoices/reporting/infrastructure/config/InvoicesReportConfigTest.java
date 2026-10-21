package com.cashly.cashly_api.finance.billing.invoices.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicePdfPort;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoice_pdf.use_case.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.generate_invoices_report_pdf.use_case.GenerateInvoicesReportPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoicesReportConfig Unit Tests")
class InvoicesReportConfigTest {

    @Mock
    private InvoiceReportQueryGateway getInvoiceByIdUseCase;

    @Mock
    private InvoicePdfPort invoicePdfPort;

    @Mock
    private InvoicesReportPdfPort invoicesReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new InvoicesReportConfig();

        var generateInvoicePdf = config.generateInvoicePdfUseCase(getInvoiceByIdUseCase, invoicePdfPort);
        assertNotNull(generateInvoicePdf);

        var generateReportPdf = config.generateInvoicesReportPdfUseCase(getInvoiceByIdUseCase, invoicesReportPdfPort);
        assertNotNull(generateReportPdf);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new InvoicesReportConfig();

        assertNotNull(config.invoicePdfPort(new PdfBranding()));
        assertNotNull(config.invoicesReportPdfPort(new PdfBranding()));
    }
}
