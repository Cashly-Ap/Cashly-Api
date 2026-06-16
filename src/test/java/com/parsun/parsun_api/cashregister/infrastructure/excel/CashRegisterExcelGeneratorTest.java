package com.parsun.parsun_api.cashregister.infrastructure.excel;

import com.parsun.parsun_api.cashregister.application.dto.CashRegisterExportRow;
import com.parsun.parsun_api.cashregister.application.dto.CustomerSummary;
import com.parsun.parsun_api.cashregister.application.dto.DailyTotals;
import com.parsun.parsun_api.cashregister.domain.entities.CashRegisterDetail;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CashRegisterExcelGenerator Unit Tests")
class CashRegisterExcelGeneratorTest {

    private CashRegisterExcelGenerator generator;

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 6, 15);
    private static final String MODULE_VENTAS = "VENTAS";
    private static final String MODULE_CXC = "CUENTAS POR COBRAR";
    private static final String MODULE_CAJA = "OPERACIONES DE CAJA";

    @BeforeEach
    void setUp() {
        generator = new CashRegisterExcelGenerator();
    }

    @Test
    @DisplayName("Should generate Excel with data rows grouped by module")
    void shouldGenerateExcelWithDataRows() {
        List<CashRegisterExportRow> rows = List.of(
                CashRegisterExportRow.of(TEST_DATE, "FAC", "INV-001", BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(500), BigDecimal.valueOf(300), BigDecimal.valueOf(200),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Cliente A",
                        MODULE_VENTAS, 1),
                CashRegisterExportRow.of(TEST_DATE, "FAC", "INV-002", BigDecimal.valueOf(2000),
                        BigDecimal.valueOf(2000), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Cliente B",
                        MODULE_VENTAS, 2),
                CashRegisterExportRow.of(TEST_DATE, "PAG", "PAY-001", BigDecimal.valueOf(500),
                        BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Cliente C",
                        MODULE_CXC, 1001),
                CashRegisterExportRow.of(TEST_DATE, "NC", "NC-001", BigDecimal.valueOf(150),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(150), "Cliente D",
                        MODULE_CAJA, 2001)
        );

        byte[] excelBytes = generator.generateExcel(rows);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        verifyExcelContent(excelBytes, 4);
    }

    @Test
    @DisplayName("Should generate Excel with no data message when no rows")
    void shouldGenerateExcelWithNoDataMessage() {
        byte[] excelBytes = generator.generateExcel(List.of());

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        verifyExcelContent(excelBytes, 0);
    }

    @Test
    @DisplayName("Should handle single module data correctly")
    void shouldHandleSingleModule() {
        List<CashRegisterExportRow> rows = List.of(
                CashRegisterExportRow.of(TEST_DATE, "FAC", "INV-001", BigDecimal.valueOf(1500),
                        BigDecimal.valueOf(1500), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Test",
                        MODULE_VENTAS, 1)
        );

        byte[] excelBytes = generator.generateExcel(rows);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    @DisplayName("Should handle REI (Reintegro) document type")
    void shouldHandleReintegroDocumentType() {
        List<CashRegisterExportRow> rows = List.of(
                CashRegisterExportRow.of(TEST_DATE, "REI", "REI-001", BigDecimal.valueOf(100),
                        BigDecimal.valueOf(100), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Cliente Reintegro",
                        MODULE_CAJA, 1)
        );

        byte[] excelBytes = generator.generateExcel(rows);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    @DisplayName("Should handle all payment method distributions")
    void shouldHandleAllPaymentMethods() {
        List<CashRegisterExportRow> rows = List.of(
                CashRegisterExportRow.of(TEST_DATE, "FAC", "INV-001", BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(1000), BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(500), BigDecimal.valueOf(500), "Multi Pago",
                        MODULE_VENTAS, 1)
        );

        byte[] excelBytes = generator.generateExcel(rows);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    @DisplayName("Should generate simple Excel without errors")
    void shouldGenerateSimpleExcel() {
        var detail1 = CashRegisterDetail.create(
                UUID.randomUUID(), "Efectivo", 5, BigDecimal.valueOf(2500));
        var detail2 = CashRegisterDetail.create(
                UUID.randomUUID(), "Transferencia", 3, BigDecimal.valueOf(1500));
        var details = List.of(detail1, detail2);

        var totals = new DailyTotals(
                BigDecimal.valueOf(4000), 8,
                BigDecimal.ZERO, 0,
                BigDecimal.valueOf(1000), 2,
                BigDecimal.valueOf(300), BigDecimal.ZERO,
                BigDecimal.valueOf(3500), 5,
                BigDecimal.valueOf(3500),
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(1300),
                details
        );

        byte[] excelBytes = generator.generateSimpleExcel(details, totals);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    @Test
    @DisplayName("Should generate grouped Excel without errors")
    void shouldGenerateGroupedExcel() {
        List<CustomerSummary> summaries = List.of(
                new CustomerSummary("Cliente A", BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(4800), BigDecimal.valueOf(200),
                        BigDecimal.valueOf(4600)),
                new CustomerSummary("Cliente B", BigDecimal.valueOf(3000),
                        BigDecimal.valueOf(3000), BigDecimal.ZERO,
                        BigDecimal.valueOf(3000))
        );

        byte[] excelBytes = generator.generateGroupedExcel(summaries);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }

    private void verifyExcelContent(byte[] excelBytes, int expectedDataRows) {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);

            assertEquals("CIERRE DE CAJA DETALLADO CON DIVISAS",
                    sheet.getRow(0).getCell(0).getStringCellValue().trim());

            if (expectedDataRows > 0) {
                assertEquals("Fecha Emisión",
                        sheet.getRow(2).getCell(0).getStringCellValue());

                assertTrue(sheet.getLastRowNum() >= 5);
            } else {
                int lastRow = sheet.getLastRowNum();
                assertTrue(lastRow >= 2);
                assertTrue(sheet.getRow(2).getCell(0).getStringCellValue()
                        .contains("No se encontraron"));
            }
        } catch (Exception e) {
            fail("Should not throw: " + e.getMessage());
        }
    }
}
