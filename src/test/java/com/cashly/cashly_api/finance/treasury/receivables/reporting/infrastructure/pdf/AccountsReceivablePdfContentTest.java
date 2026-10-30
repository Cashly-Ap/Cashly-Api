package com.cashly.cashly_api.finance.treasury.receivables.reporting.infrastructure.pdf;
import com.cashly.cashly_api.finance.treasury.receivables.reporting.application.dto.model.AccountsReceivableReportData;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class AccountsReceivablePdfContentTest {
    private BigDecimal amount(int value) { return BigDecimal.valueOf(value); }
    private String text(byte[] bytes) throws Exception {
        try (var pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            var text = new StringBuilder();
            for (int i = 1; i <= pdf.getNumberOfPages(); i++) text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)));
            return text.toString();
        }
    }
    @Test void reportPreservesCustomerCreditAgingAndSummaryAmounts() throws Exception {
        var row = new AccountsReceivableReportData.AccountReceivableRow(UUID.randomUUID(), "Customer Example", "RIF-123", amount(100), amount(120), amount(-20), amount(30), amount(20), amount(10), amount(40), amount(20));
        var data = new AccountsReceivableReportData(LocalDate.of(2026, 1, 2), true, List.of(row), new AccountsReceivableReportData.Summary(amount(120), amount(30), amount(90), 1));
        var content = text(new AccountsReceivableReportPdfGenerator().generateReport(data));
        for (var expected : List.of("Customer Example", "RIF-123", "100,00", "120,00", "-20,00", "30,00", "90,00", "Total Cuentas por Cobrar", "Por Vencer", "Vencidas")) assertTrue(content.contains(expected), expected);
    }
    @Test void emptyReportPreservesZeroSummary() throws Exception {
        var data = new AccountsReceivableReportData(LocalDate.of(2026, 1, 2), false, List.of(), new AccountsReceivableReportData.Summary(amount(0), amount(0), amount(0), 0));
        var content = text(new AccountsReceivableReportPdfGenerator().generateReport(data));
        assertTrue(content.contains("0,00"));
        assertTrue(content.contains("Total Cuentas por Cobrar"));
    }
}
