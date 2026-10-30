package com.cashly.cashly_api.finance.treasury.payments.reporting.infrastructure.pdf;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.dto.model.PaymentReportData;
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
class PaymentPdfContentTest {
    private String text(byte[] bytes) throws Exception {
        try (var pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            var text = new StringBuilder();
            for (int i = 1; i <= pdf.getNumberOfPages(); i++) text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)));
            return text.toString();
        }
    }
    @Test
    void paymentPdfPreservesNumberDateAmountReferenceStatusAndNotes() throws Exception {
        var data = mock(PaymentReportData.class);
        when(data.paymentNumber()).thenReturn("PAY-123");
        when(data.createdAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 30));
        when(data.amount()).thenReturn(new BigDecimal("123.45"));
        when(data.referenceNumber()).thenReturn("REF-123");
        when(data.status()).thenReturn("VERIFIED");
        when(data.notes()).thenReturn("Payment instructions");
        var content = text(new PaymentPdfGenerator().generatePaymentPdf(data));
        for (var expected : List.of("PAY-123", "02/01/2026 10:30", "123,45", "REF-123", "VERIFIED", "Payment instructions")) assertTrue(content.contains(expected), expected + " missing: " + content);
    }
    @Test
    void paymentPdfSupportsMissingReferenceStatusAndNotes() throws Exception {
        var data = mock(PaymentReportData.class);
        when(data.paymentNumber()).thenReturn("PAY-EMPTY");
        when(data.createdAt()).thenReturn(LocalDateTime.of(2026, 1, 2, 10, 30));
        when(data.amount()).thenReturn(BigDecimal.ZERO);
        var content = text(new PaymentPdfGenerator().generatePaymentPdf(data));
        assertTrue(content.contains("PAY-EMPTY"));
        assertTrue(content.contains("0,00"));
        assertTrue(content.contains("-"));
        assertFalse(content.contains("OBSERVACIONES"));
    }
}
