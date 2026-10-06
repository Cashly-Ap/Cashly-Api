package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicesReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateInvoicesReportPdfUseCaseImpl Unit Tests")
class GenerateInvoicesReportPdfUseCaseImplTest {

    @Mock
    private InvoicesReportPdfPort invoicesReportPdfPort;

    @InjectMocks
    private GenerateInvoicesReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate invoices report PDF with SIMPLE mode")
    void should_generate_invoices_report_pdf() {
        List<InvoiceListResponse> invoices = List.of(mock(InvoiceListResponse.class));
        byte[] expectedPdf = "Report PDF".getBytes();

        when(invoicesReportPdfPort.generatePdf(invoices, "SIMPLE")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(invoices, "SIMPLE");

        assertArrayEquals(expectedPdf, result);
        verify(invoicesReportPdfPort).generatePdf(invoices, "SIMPLE");
    }

    @Test
    @DisplayName("should generate invoices report PDF with null mode defaulting to simple")
    void should_generate_invoices_report_pdf_with_null_mode() {
        List<InvoiceListResponse> invoices = List.of();
        byte[] expectedPdf = "Default PDF".getBytes();

        when(invoicesReportPdfPort.generatePdf(invoices, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(invoices, null);

        assertArrayEquals(expectedPdf, result);
        verify(invoicesReportPdfPort).generatePdf(invoices, "simple");
    }
}
