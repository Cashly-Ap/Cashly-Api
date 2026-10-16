package com.cashly.cashly_api.invoices.reporting.application.generate_invoices_report_pdf.services;

import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportRow;
import com.cashly.cashly_api.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import com.cashly.cashly_api.invoices.reporting.application.ports.out.InvoiceReportQueryGateway;
import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateInvoicesReportPdfUseCaseImpl Unit Tests")
class GenerateInvoicesReportPdfUseCaseImplTest {

    @Mock
    private InvoicesReportPdfPort invoicesReportPdfPort;

    @Mock private InvoiceReportQueryGateway query;
    @InjectMocks
    private GenerateInvoicesReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate invoices report PDF with SIMPLE mode")
    void should_generate_invoices_report_pdf() {
        List<InvoiceReportRow> invoices = List.of(mock(InvoiceReportRow.class));
        InvoiceReportFilter filter = new InvoiceReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(invoices);
        byte[] expectedPdf = "Report PDF".getBytes();

        when(invoicesReportPdfPort.generatePdf(invoices, "SIMPLE")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, "SIMPLE");

        assertArrayEquals(expectedPdf, result);
        verify(invoicesReportPdfPort).generatePdf(invoices, "SIMPLE");
    }

    @Test
    @DisplayName("should generate invoices report PDF with null mode defaulting to simple")
    void should_generate_invoices_report_pdf_with_null_mode() {
        List<InvoiceReportRow> invoices = List.of();
        InvoiceReportFilter filter = new InvoiceReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(invoices);
        byte[] expectedPdf = "Default PDF".getBytes();

        when(invoicesReportPdfPort.generatePdf(invoices, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, null);

        assertArrayEquals(expectedPdf, result);
        verify(invoicesReportPdfPort).generatePdf(invoices, "simple");
    }
}
