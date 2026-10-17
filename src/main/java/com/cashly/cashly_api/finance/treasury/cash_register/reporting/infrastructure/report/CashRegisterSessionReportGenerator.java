package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.report;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.response.CashMovementResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.dto.response.CashRegisterResponse;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model.ReportType;
import com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.ports.out.CashRegisterReportPort;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Component
public class CashRegisterSessionReportGenerator implements CashRegisterReportPort {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generateExcel(CashRegisterResponse register, ReportType type) {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var summary = workbook.createSheet("Resumen");
            CellStyle header = headerStyle(workbook);
            addSummaryRow(summary.createRow(0), "Reporte de caja", register.id().toString(), header);
            addSummaryRow(summary.createRow(1), "Estado", register.status().name(), null);
            addSummaryRow(summary.createRow(2), "Apertura", format(register.openedAt()), null);
            addSummaryRow(summary.createRow(3), "Cierre", format(register.closedAt()), null);
            addMoneyRow(summary.createRow(4), "Facturación neta", register.totalInvoiced());
            addMoneyRow(summary.createRow(5), "Saldo inicial", register.openingBalance());
            addMoneyRow(summary.createRow(6), "Ingresos", register.totalIncome());
            addMoneyRow(summary.createRow(7), "Egresos", register.totalExpense());
            addMoneyRow(summary.createRow(8), "Saldo esperado", register.expectedBalance());
            addMoneyRow(summary.createRow(9), "Saldo declarado", register.declaredBalance());
            addMoneyRow(summary.createRow(10), "Diferencia", register.difference());
            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            if (type == ReportType.DETAILED) {
                addMovementSheet(workbook, register, header);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate cash register Excel report", exception);
        }
    }

    @Override
    public byte[] generatePdf(CashRegisterResponse register, ReportType type) {
        try (var output = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            Document document = new Document(pdf, PageSize.A4.rotate());
            var bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.add(new Paragraph("Reporte de caja").setFont(bold).setFontSize(18));
            document.add(new Paragraph("Sesión: " + register.id()));
            document.add(new Paragraph("Apertura: " + format(register.openedAt())
                    + "   Cierre: " + format(register.closedAt())));

            Table summary = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
            addPdfSummary(summary, "Facturación neta", register.totalInvoiced());
            addPdfSummary(summary, "Saldo inicial", register.openingBalance());
            addPdfSummary(summary, "Ingresos", register.totalIncome());
            addPdfSummary(summary, "Egresos", register.totalExpense());
            addPdfSummary(summary, "Saldo esperado", register.expectedBalance());
            addPdfSummary(summary, "Saldo declarado", register.declaredBalance());
            addPdfSummary(summary, "Diferencia", register.difference());
            document.add(summary);

            if (type == ReportType.DETAILED) {
                document.add(new Paragraph("Movimientos").setFont(bold).setFontSize(14));
                Table details = new Table(UnitValue.createPercentArray(
                        new float[]{1.4f, 1.1f, 1.2f, 1.2f, 1.2f, 2.4f, 1.5f})).useAllAvailableWidth();
                for (String title : new String[]{"Fecha", "Dirección", "Tipo", "Método", "Importe", "Descripción", "Usuario"}) {
                    details.addHeaderCell(new Cell().add(new Paragraph(title).setFont(bold)));
                }
                for (CashMovementResponse movement : register.movements()) {
                    details.addCell(format(movement.occurredAt()));
                    details.addCell(movement.direction().name());
                    details.addCell(movement.type().name());
                    details.addCell(value(movement.paymentMethodName()));
                    details.addCell(money(movement.amount()));
                    details.addCell(value(movement.description()));
                    details.addCell(value(movement.createdByName()));
                }
                document.add(details);
            }
            document.close();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate cash register PDF report", exception);
        }
    }

    private void addMovementSheet(XSSFWorkbook workbook, CashRegisterResponse register, CellStyle header) {
        var sheet = workbook.createSheet("Detalle");
        Row titles = sheet.createRow(0);
        String[] columns = {"Fecha", "Dirección", "Tipo", "Método", "Importe", "Descripción", "Origen", "Usuario"};
        for (int i = 0; i < columns.length; i++) {
            titles.createCell(i).setCellValue(columns[i]);
            titles.getCell(i).setCellStyle(header);
        }
        int rowIndex = 1;
        for (CashMovementResponse movement : register.movements()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(format(movement.occurredAt()));
            row.createCell(1).setCellValue(movement.direction().name());
            row.createCell(2).setCellValue(movement.type().name());
            row.createCell(3).setCellValue(value(movement.paymentMethodName()));
            row.createCell(4).setCellValue(movement.amount().doubleValue());
            row.createCell(5).setCellValue(value(movement.description()));
            row.createCell(6).setCellValue(value(movement.sourceType()));
            row.createCell(7).setCellValue(value(movement.createdByName()));
        }
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private void addSummaryRow(Row row, String label, String value, CellStyle style) {
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        if (style != null) {
            row.getCell(0).setCellStyle(style);
            row.getCell(1).setCellStyle(style);
        }
    }

    private void addMoneyRow(Row row, String label, BigDecimal amount) {
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(orZero(amount).doubleValue());
    }

    private void addPdfSummary(Table table, String label, BigDecimal amount) {
        table.addCell(label);
        table.addCell(money(amount));
    }

    private String format(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "—" : DATE_TIME.format(dateTime);
    }

    private String money(BigDecimal amount) {
        return orZero(amount).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal orZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String value(String text) {
        return text == null ? "—" : text;
    }
}
