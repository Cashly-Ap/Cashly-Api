package com.cashly.cashly_api.reports.invoices.infrastructure.config;

import com.cashly.cashly_api.invoices.application.usecases.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicePdfPort;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicesReportPdfPort;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicePdfUseCase;
import com.cashly.cashly_api.reports.invoices.application.usecases.GenerateInvoicesReportPdfUseCase;
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
    private GetInvoiceByIdUseCase getInvoiceByIdUseCase;

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

        var generateReportPdf = config.generateInvoicesReportPdfUseCase(invoicesReportPdfPort);
        assertNotNull(generateReportPdf);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new InvoicesReportConfig();

        assertNotNull(config.invoicePdfPort());
        assertNotNull(config.invoicesReportPdfPort());
    }
}
