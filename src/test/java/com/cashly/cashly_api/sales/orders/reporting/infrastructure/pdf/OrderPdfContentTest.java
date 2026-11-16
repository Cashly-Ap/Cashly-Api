package com.cashly.cashly_api.sales.orders.reporting.infrastructure.pdf;
import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportData;
import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportRow;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrderPdfContentTest {
    private String text(byte[] bytes) throws Exception {
        try (var pdf = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(bytes)))) {
            var text = new StringBuilder();
            for (int i = 1; i <= pdf.getNumberOfPages(); i++)
                text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)));
            return text.toString();
        }
    }
    @Test
    void documentPreservesCustomerSubtotalNotesAndCancellation()
            throws Exception {
        var data = mock(OrderReportData.class);
        when(data.orderNumber()).thenReturn("DOC-123");
        when(data.customerName()).thenReturn("Customer Example");
        when(data.customerRif()).thenReturn("RIF-123");
        when(data.status()).thenReturn("CANCELLED");
        when(data.notes()).thenReturn("Delivery instructions");
        when(data.cancelledReason()).thenReturn("Cancellation reason");
        when(data.subtotal()).thenReturn(new BigDecimal("27.90"));
        when(data.createdAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 30));
        var line = mock(OrderReportData.Line.class);
        when(line.productCode()).thenReturn("P-123");
        when(line.productName()).thenReturn("Product Example");
        when(line.quantity()).thenReturn(new BigDecimal("2.25"));
        when(line.unitPrice()).thenReturn(new BigDecimal("12.40"));
        when(line.lineTotal()).thenReturn(new BigDecimal("27.90"));
        when(data.lines()).thenReturn(List.of(line));
        var content = text(new OrderPdfGenerator().generatePdf(data));
        for (var expected : List.of("DOC-123", "Customer Example", "RIF-123",
                "27,90", "Delivery instructions", "Cancellation reason",
                "P-123", "Product Example", "2,25", "12,40"))
            assertTrue(content.contains(expected),
                    expected + " missing: " + content);
    }
    @Test
    void listPreservesSimpleDetailedAndEmptyContent() throws Exception {
        var row = mock(OrderReportRow.class);
        when(row.orderNumber()).thenReturn("ROW-123");
        when(row.customerName()).thenReturn("Report Customer");
        when(row.subtotal()).thenReturn(new BigDecimal("31.25"));
        when(row.status()).thenReturn("DRAFT");
        when(row.createdAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 30));
        var generator = new OrdersReportPdfGenerator();
        var simple = text(generator.generatePdf(List.of(row), "SIMPLE"));
        var detailed = text(generator.generatePdf(List.of(row), "detailed"));
        for (var content : List.of(simple, detailed))
            for (var expected : List.of("ROW-123", "Report Customer", "31,25",
                    "Borrador: 1"))
                assertTrue(content.contains(expected),
                        expected + " missing: " + content);
        assertTrue(simple.contains("Modo: Simple"));
        assertTrue(detailed.contains("Modo: Detallado"));
        assertTrue(text(generator.generatePdf(List.of(), "SIMPLE"))
                .contains("Total: 0"));
    }
    @Test
    void reportEmbedsConfiguredCompanyLogoInsteadOfBundledBranding()
            throws Exception {
        var image = new java.awt.image.BufferedImage(73, 29,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        var buffer = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", buffer);
        var generator = new OrdersReportPdfGenerator(
                new com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding(
                        buffer::toByteArray));
        try (var pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(
                generator.generatePdf(List.of(), "SIMPLE"))))) {
            var resources = pdf.getFirstPage().getResources();
            var objects = resources
                    .getResource(com.itextpdf.kernel.pdf.PdfName.XObject);
            assertNotNull(objects);
            boolean customLogoFound = false;
            for (var name : objects.keySet()) {
                var stream = objects.getAsStream(name);
                if (com.itextpdf.kernel.pdf.PdfName.Image.equals(stream
                        .getAsName(com.itextpdf.kernel.pdf.PdfName.Subtype))) {
                    var embedded = new com.itextpdf.kernel.pdf.xobject.PdfImageXObject(
                            stream).getBufferedImage();
                    customLogoFound |= embedded.getWidth() == 73
                            && embedded.getHeight() == 29;
                }
            }
            assertTrue(customLogoFound,
                    "Configured company PDF logo must be embedded in the generated report");
        }
    }
}
