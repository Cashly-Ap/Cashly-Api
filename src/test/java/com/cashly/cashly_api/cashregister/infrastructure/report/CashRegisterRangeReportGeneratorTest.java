package com.cashly.cashly_api.cashregister.infrastructure.report;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cashregister.application.dto.CashRegisterReportFilter;
import com.cashly.cashly_api.cashregister.application.dto.ReportFormat;
import com.cashly.cashly_api.cashregister.application.dto.ReportType;
import com.cashly.cashly_api.cashregister.application.port.CashRegisterExcelPort;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashRegisterRangeReportGeneratorTest {

    private final CashRegisterExcelPort excelPort = mock(CashRegisterExcelPort.class);
    private final CashRegisterRangeReportGenerator generator = new CashRegisterRangeReportGenerator(excelPort);
    private final CashRegisterReportFilter filter = new CashRegisterReportFilter(
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), null, null, null);

    @Test
    void should_generate_grouped_pdf() {
        byte[] result = generator.generate(rows(), filter, ReportFormat.PDF, ReportType.GROUPED);

        assertTrue(new String(result, 0, 4).startsWith("%PDF"));
    }

    @Test
    void should_format_and_sort_spanish_dates_without_module_in_detailed_pdf() throws Exception {
        List<CashRegisterExportRow> unsortedRows = List.of(
                row(LocalDate.of(2026, 8, 14), "CN-2026-0001", "OPERACIONES DE CAJA", 2),
                row(LocalDate.of(2026, 8, 7), "INV-2026-00044", "VENTAS", 1));

        byte[] result = generator.generate(unsortedRows, filter, ReportFormat.PDF, ReportType.DETAILED);

        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(result)))) {
            String text = IntStream.rangeClosed(1, pdf.getNumberOfPages())
                    .mapToObj(page -> PdfTextExtractor.getTextFromPage(pdf.getPage(page)))
                    .reduce("", String::concat);
            assertTrue(text.contains("07/08/2026"));
            assertTrue(text.contains("14/08/2026"));
            assertTrue(text.indexOf("07/08/2026") < text.indexOf("14/08/2026"));
            assertFalse(text.contains("2026-08"));
            assertFalse(text.contains("Módulo"));
            assertFalse(text.contains("OPERACIONES DE CAJA"));
            assertFalse(text.contains("VENTAS"));
        }
    }

    @Test
    void should_generate_detailed_excel() {
        when(excelPort.generateExcel(anyList())).thenReturn(new byte[]{1});

        generator.generate(rows(), filter, ReportFormat.EXCEL, ReportType.DETAILED);

        verify(excelPort).generateExcel(rows());
    }

    private List<CashRegisterExportRow> rows() {
        return List.of(row(LocalDate.of(2026, 8, 10), "INV-1", "VENTAS", 1));
    }

    private CashRegisterExportRow row(LocalDate date, String documentNumber, String module, int sortOrder) {
        return CashRegisterExportRow.of(
                date, "FAC", documentNumber, new BigDecimal("100.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "Cliente", module, sortOrder);
    }
}
