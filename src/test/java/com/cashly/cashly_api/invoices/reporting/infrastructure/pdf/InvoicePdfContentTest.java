package com.cashly.cashly_api.invoices.reporting.infrastructure.pdf;

import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportData;
import com.cashly.cashly_api.invoices.reporting.application.dto.model.InvoiceReportRow;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoicePdfContentTest {
    private static String text(byte[] bytes) throws Exception {
        try (var pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= pdf.getNumberOfPages(); i++) text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)));
            return text.toString();
        }
    }
    @Test
    void invoicePdfPreservesCustomerLinesAmountsAndCancellation() throws Exception {
        var invoice = mock(InvoiceReportData.class);
        var line = mock(InvoiceReportData.Line.class);
        when(invoice.invoiceNumber()).thenReturn("INV-CONTENT");
        when(invoice.customerName()).thenReturn("Customer Example");
        when(invoice.customerRif()).thenReturn("RIF-123");
        when(invoice.orderNumber()).thenReturn("ORD-123");
        when(invoice.createdAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 30));
        when(invoice.status()).thenReturn("CANCELLED");
        when(invoice.notes()).thenReturn("Delivery instructions");
        when(invoice.cancelledReason()).thenReturn("Cancellation reason");
        when(invoice.paymentType()).thenReturn("CREDIT");
        when(invoice.subtotal()).thenReturn(new BigDecimal("27.90"));
        when(invoice.total()).thenReturn(new BigDecimal("27.90"));
        when(invoice.lines()).thenReturn(List.of(line));
        when(line.productCode()).thenReturn("P-123");
        when(line.productName()).thenReturn("Product Example");
        when(line.quantity()).thenReturn(new BigDecimal("2.25"));
        when(line.unitPrice()).thenReturn(new BigDecimal("12.40"));
        when(line.lineTotal()).thenReturn(new BigDecimal("27.90"));
        var content = text(new InvoicePdfGenerator().generatePdf(invoice));
        for (var expected : List.of("INV-CONTENT", "Customer Example", "RIF-123", "ORD-123", "02/01/2026 10:30", "P-123", "Product Example", "2.25", "12.40", "27.90", "CREDITO", "Delivery instructions", "Cancellation reason")) assertTrue(content.contains(expected), expected + " missing from " + content);
    }
    @Test
    void reportPdfPreservesSimpleDetailedAndEmptyModes() throws Exception {
        var row = mock(InvoiceReportRow.class);
        when(row.invoiceNumber()).thenReturn("INV-REPORT");
        when(row.customerName()).thenReturn("Report Customer");
        when(row.total()).thenReturn(new BigDecimal("31.25"));
        when(row.createdAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 30));
        when(row.status()).thenReturn("PAID");
        when(row.paymentType()).thenReturn("CASH");
        when(row.lineCount()).thenReturn(2);
        var generator = new InvoicesReportPdfGenerator();
        var simple = text(generator.generatePdf(List.of(row), "SIMPLE"));
        var detailed = text(generator.generatePdf(List.of(row), "detailed"));
        for (var content : List.of(simple, detailed)) for (var expected : List.of("INV-REPORT", "Report Customer", "31.25", "02/01/2026 10:30", "Pagadas: 1")) assertTrue(content.contains(expected), expected);
        assertTrue(simple.contains("Modo: Simple"));
        assertTrue(detailed.contains("Modo: Detallado"));
        assertTrue(detailed.contains("CONTADO"));
        assertTrue(text(generator.generatePdf(List.of(), "SIMPLE")).contains("Total: 0 facturas"));
    }
}
