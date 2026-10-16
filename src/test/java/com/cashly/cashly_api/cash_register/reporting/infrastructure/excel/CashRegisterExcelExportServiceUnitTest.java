package com.cashly.cashly_api.cash_register.reporting.infrastructure.excel;

import com.cashly.cashly_api.cash_register.reporting.application.dto.CashRegisterExportRow;
import com.cashly.cashly_api.cash_register.application.ports.out.CashRegisterQueryGateway;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashRegisterExcelExportService Unit Tests")
class CashRegisterExcelExportServiceUnitTest {

    @Mock
    private CashRegisterQueryGateway repository;

    private CashRegisterExcelExportService service;

    private static final LocalDate TEST_DATE = LocalDate.of(2025, 6, 15);
    private static final String MODULE_VENTAS = "VENTAS";
    private static final String MODULE_CXC = "CUENTAS POR COBRAR";
    private static final String MODULE_CAJA = "OPERACIONES DE CAJA";

    @BeforeEach
    void setUp() {
        service = new CashRegisterExcelExportService(repository);
    }

    @Test
    @DisplayName("Should generate Excel with data rows grouped by module")
    void shouldGenerateExcelWithDataRows() {
        List<CashRegisterExportRow> rows = List.of(
                CashRegisterExportRow.of(TEST_DATE, "FAC", "INV-001", BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(500), BigDecimal.valueOf(300), BigDecimal.valueOf(200),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Cliente A", MODULE_VENTAS, 1),
                CashRegisterExportRow.of(TEST_DATE, "FAC", "INV-002", BigDecimal.valueOf(2000),
                        BigDecimal.valueOf(2000), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Cliente B", MODULE_VENTAS, 2),
                CashRegisterExportRow.of(TEST_DATE, "PAG", "PAY-001", BigDecimal.valueOf(500),
                        BigDecimal.valueOf(500), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Cliente C", MODULE_CXC, 1001),
                CashRegisterExportRow.of(TEST_DATE, "NC", "NC-001", BigDecimal.valueOf(150),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(150), BigDecimal.ZERO,
                        "Cliente D", MODULE_CAJA, 2001)
        );

        when(repository.findExportRows(TEST_DATE, TEST_DATE)).thenReturn(rows);

        byte[] excelBytes = service.exportToExcel(TEST_DATE, TEST_DATE);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        verifyExcelContent(excelBytes, 4);
    }

    @Test
    @DisplayName("Should generate Excel with no data message when no rows")
    void shouldGenerateExcelWithNoDataMessage() {
        when(repository.findExportRows(TEST_DATE, TEST_DATE)).thenReturn(List.of());

        byte[] excelBytes = service.exportToExcel(TEST_DATE, TEST_DATE);

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
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        "Test", MODULE_VENTAS, 1)
        );

        when(repository.findExportRows(TEST_DATE, TEST_DATE)).thenReturn(rows);

        byte[] excelBytes = service.exportToExcel(TEST_DATE, TEST_DATE);

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
                        BigDecimal.valueOf(500), BigDecimal.valueOf(500), BigDecimal.ZERO,
                        "Multi Pago", MODULE_VENTAS, 1)
        );

        when(repository.findExportRows(TEST_DATE, TEST_DATE)).thenReturn(rows);

        byte[] excelBytes = service.exportToExcel(TEST_DATE, TEST_DATE);

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
