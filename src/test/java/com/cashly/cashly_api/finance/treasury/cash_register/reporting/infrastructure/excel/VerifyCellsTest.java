package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.excel;

import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CashRegisterExportRow;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.CustomerSummary;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.DailyTotals;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegisterDetail;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class VerifyCellsTest {
    private final CashRegisterExcelGenerator gen = new CashRegisterExcelGenerator();

    @Test
    @DisplayName("Simple report payment method names appear in cells")
    void simpleReportNames() throws Exception {
        var d1 = CashRegisterDetail.create(UUID.randomUUID(), "Efectivo", 5,
                BigDecimal.valueOf(2500));
        var d2 = CashRegisterDetail.create(UUID.randomUUID(), "Transferencia",
                3, BigDecimal.valueOf(1500));
        var details = List.of(d1, d2);
        var totals = new DailyTotals(BigDecimal.valueOf(4000), 8,
                BigDecimal.ZERO, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.valueOf(4000), 8,
                BigDecimal.valueOf(4000), BigDecimal.valueOf(4000),
                BigDecimal.ZERO, details);
        byte[] excelBytes = gen.generateSimpleExcel(details, totals);
        try (Workbook wb = new XSSFWorkbook(
                new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getSheetAt(0);

            String row4name = getCellString(sheet, 4, 0);
            String row5name = getCellString(sheet, 5, 0);

            assertEquals("Efectivo", row4name,
                    "First payment method name should appear");
            assertEquals("Transferencia", row5name,
                    "Second payment method name should appear");
        }
    }

    @Test
    @DisplayName("Grouped report customer names appear in cells")
    void groupedReportNames() throws Exception {
        var summaries = List.of(
                new CustomerSummary("Cliente A", BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(4800), BigDecimal.valueOf(200),
                        BigDecimal.valueOf(4600)),
                new CustomerSummary("Cliente B", BigDecimal.valueOf(3000),
                        BigDecimal.valueOf(3000), BigDecimal.ZERO,
                        BigDecimal.valueOf(3000)));
        byte[] excelBytes = gen.generateGroupedExcel(summaries);
        try (Workbook wb = new XSSFWorkbook(
                new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getSheetAt(0);

            String row3name = getCellString(sheet, 3, 0);
            String row4name = getCellString(sheet, 4, 0);

            assertEquals("Cliente A", row3name,
                    "First customer name should appear");
            assertEquals("Cliente B", row4name,
                    "Second customer name should appear");
        }
    }

    @Test
    @DisplayName("Detailed report has RESUMEN GENERAL section with vertical layout")
    void detailedReportHasResumenGeneral() throws Exception {
        var rows = List.of(
                CashRegisterExportRow.of(LocalDate.of(2025, 6, 15), "FAC",
                        "INV-001", BigDecimal.valueOf(1000), BigDecimal
                                .valueOf(500),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Cliente A", "VENTAS", 1),
                CashRegisterExportRow.of(LocalDate.of(2025, 6, 15), "PAG",
                        "PAY-001", BigDecimal.valueOf(500),
                        BigDecimal.valueOf(500), BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, "Cliente C",
                        "CUENTAS POR COBRAR", 1001),
                CashRegisterExportRow.of(LocalDate.of(2025, 6, 15), "NC",
                        "NC-001", BigDecimal.valueOf(150), BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.valueOf(150),
                        BigDecimal.ZERO, "Cliente D", "OPERACIONES DE CAJA",
                        2001));
        byte[] excelBytes = gen.generateExcel(rows);
        try (Workbook wb = new XSSFWorkbook(
                new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = wb.getSheetAt(0);

            int summaryRow = -1;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                String val = getCellString(sheet, i, 0);
                if ("RESUMEN GENERAL".equals(val)) {
                    summaryRow = i;
                    break;
                }
            }
            assertTrue(summaryRow >= 0,
                    "RESUMEN GENERAL section title should exist");
            assertEquals("Concepto", getCellString(sheet, summaryRow + 1, 0));
            assertEquals("Monto", getCellString(sheet, summaryRow + 1, 1));
            assertEquals("Facturación neta",
                    getCellString(sheet, summaryRow + 2, 0));
            assertEquals("Ingresos", getCellString(sheet, summaryRow + 3, 0));
            assertEquals("Egresos", getCellString(sheet, summaryRow + 4, 0));
            assertEquals("Balance", getCellString(sheet, summaryRow + 5, 0));
            assertEquals(850.0, sheet.getRow(summaryRow + 2).getCell(1)
                    .getNumericCellValue());
            assertEquals(500.0, sheet.getRow(summaryRow + 3).getCell(1)
                    .getNumericCellValue());
            assertEquals(0.0, sheet.getRow(summaryRow + 4).getCell(1)
                    .getNumericCellValue());
            for (int i = 2; i <= 5; i++) {
                Cell c = sheet.getRow(summaryRow + i).getCell(1);
                assertNotNull(c);
                assertEquals(CellType.NUMERIC, c.getCellType());
            }

            System.out.println("\n=== ESTRUCTURA DETALLADA ===");
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                StringBuilder sb = new StringBuilder("Row " + i + ": ");
                for (int j = 0; j <= 4; j++) {
                    String val = getCellString(sheet, i, j);
                    sb.append("| ").append(val).append(" ");
                }
                System.out.println(sb);
            }
        }
    }

    private String getCellString(Sheet sheet, int row, int col) {
        Row r = sheet.getRow(row);
        if (r == null)
            return "NULL_ROW";
        Cell c = r.getCell(col);
        if (c == null)
            return "NULL_CELL";
        if (c.getCellType() == CellType.STRING)
            return c.getStringCellValue();
        if (c.getCellType() == CellType.NUMERIC)
            return String.valueOf(c.getNumericCellValue());
        if (c.getCellType() == CellType.BLANK)
            return "[BLANK]";
        return "[TYPE:" + c.getCellType() + "]";
    }
}
