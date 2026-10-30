package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.report;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterStatus;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CashRegisterSessionReportGeneratorTest {

    private final CashRegisterSessionReportGenerator generator = new CashRegisterSessionReportGenerator();

    @Test
    void should_generate_detailed_excel_with_movement_rows() throws Exception {
        byte[] bytes = generator.generateExcel(register(), ReportType.DETAILED);

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var summary = workbook.getSheet("Resumen");
            assertEquals("Facturación neta", summary.getRow(4).getCell(0).getStringCellValue());
            assertEquals(125.0, summary.getRow(4).getCell(1).getNumericCellValue());
            var sheet = workbook.getSheet("Detalle");
            assertEquals("Pago", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals(50.0, sheet.getRow(1).getCell(4).getNumericCellValue());
        }
    }

    @Test
    void should_generate_valid_pdf_for_simple_and_detailed_views() {
        byte[] simple = generator.generatePdf(register(), ReportType.SIMPLE);
        byte[] detailed = generator.generatePdf(register(), ReportType.DETAILED);

        assertTrue(new String(simple, 0, 4).startsWith("%PDF"));
        assertTrue(new String(detailed, 0, 4).startsWith("%PDF"));
        assertTrue(detailed.length > simple.length);
    }

    private CashRegisterResponse register() {
        UUID registerId = UUID.randomUUID();
        CashMovementResponse movement = new CashMovementResponse(
                UUID.randomUUID(), CashMovementDirection.IN, CashMovementType.PAYMENT,
                new BigDecimal("50.00"), UUID.randomUUID(), "Efectivo", "Cobro factura",
                "PAYMENT", UUID.randomUUID(), UUID.randomUUID(), "Operator", LocalDateTime.now());
        return new CashRegisterResponse(
                registerId, LocalDate.now(), RegisterStatus.CLOSED,
                UUID.randomUUID(), "Operator", LocalDateTime.now().minusHours(8),
                UUID.randomUUID(), "Operator", LocalDateTime.now(), "Sin incidencias",
                List.of(), new BigDecimal("125.00"), new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("50.00"), List.of(), List.of(),
                new BigDecimal("100.00"), new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("150.00"), new BigDecimal("150.00"), BigDecimal.ZERO,
                List.of(movement));
    }
}
