package com.cashly.cashly_api.finance.treasury.cash_register.reporting.infrastructure.report;

import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CashRegisterSessionReportGenerator
        implements
            CashRegisterReportPort {

    private final PdfBranding branding;

    public CashRegisterSessionReportGenerator() {
        this(new PdfBranding());
    }

    @Autowired
    public CashRegisterSessionReportGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generateExcel(CashRegisterResponse register,
            ReportType type) {
        try (var workbook = new XSSFWorkbook();
                var output = new ByteArrayOutputStream()) {
            var summary = workbook.createSheet(ReportMessages
                    .text("finance.treasury.cash_register.reports.summary"));
            CellStyle header = headerStyle(workbook);
            addSummaryRow(summary.createRow(0), ReportMessages.text(
                    "finance.treasury.cash_register.reports.cashRegisterReport"),
                    register.id().toString(), header);
            addSummaryRow(summary.createRow(1),
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.status"),
                    ReportMessages.code(register.status().name()), null);
            addSummaryRow(summary.createRow(2),
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.opened"),
                    format(register.openedAt()), null);
            addSummaryRow(summary.createRow(3),
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.closed"),
                    format(register.closedAt()), null);
            addMoneyRow(summary.createRow(4), ReportMessages.text(
                    "finance.treasury.cash_register.reports.netInvoicing"),
                    register.totalInvoiced());
            addMoneyRow(summary.createRow(5), ReportMessages.text(
                    "finance.treasury.cash_register.reports.openingBalance"),
                    register.openingBalance());
            addMoneyRow(summary.createRow(6),
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.inflows"),
                    register.totalIncome());
            addMoneyRow(summary.createRow(7),
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.outflows"),
                    register.totalExpense());
            addMoneyRow(summary.createRow(8), ReportMessages.text(
                    "finance.treasury.cash_register.reports.expectedBalance"),
                    register.expectedBalance());
            addMoneyRow(summary.createRow(9), ReportMessages.text(
                    "finance.treasury.cash_register.reports.declaredBalance"),
                    register.declaredBalance());
            addMoneyRow(summary.createRow(10), ReportMessages
                    .text("finance.treasury.cash_register.reports.difference"),
                    register.difference());
            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            if (type == ReportType.DETAILED) {
                addMovementSheet(workbook, register, header);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate cash register Excel report", exception);
        }
    }

    @Override
    public byte[] generatePdf(CashRegisterResponse register, ReportType type) {
        try (var output = new ByteArrayOutputStream()) {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            Document document = new Document(pdf, PageSize.A4.rotate());
            var bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            document.add(branding.logo().setMarginBottom(12));
            document.add(new Paragraph(ReportMessages.text(
                    "finance.treasury.cash_register.reports.cashRegisterReport"))
                    .setFont(bold).setFontSize(18));
            document.add(new Paragraph(ReportMessages
                    .text("finance.treasury.cash_register.reports.session")
                    + register.id()));
            document.add(new Paragraph(ReportMessages
                    .text("finance.treasury.cash_register.reports.opened")
                    + format(register.openedAt())
                    + ReportMessages.text(
                            "finance.treasury.cash_register.reports.closed")
                    + format(register.closedAt())));

            Table summary = new Table(
                    UnitValue.createPercentArray(new float[]{2, 1}))
                    .useAllAvailableWidth();
            addPdfSummary(summary, ReportMessages.text(
                    "finance.treasury.cash_register.reports.netInvoicing"),
                    register.totalInvoiced());
            addPdfSummary(summary, ReportMessages.text(
                    "finance.treasury.cash_register.reports.openingBalance"),
                    register.openingBalance());
            addPdfSummary(summary,
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.inflows"),
                    register.totalIncome());
            addPdfSummary(summary,
                    ReportMessages.text(
                            "finance.treasury.cash_register.reports.outflows"),
                    register.totalExpense());
            addPdfSummary(summary, ReportMessages.text(
                    "finance.treasury.cash_register.reports.expectedBalance"),
                    register.expectedBalance());
            addPdfSummary(summary, ReportMessages.text(
                    "finance.treasury.cash_register.reports.declaredBalance"),
                    register.declaredBalance());
            addPdfSummary(summary, ReportMessages
                    .text("finance.treasury.cash_register.reports.difference"),
                    register.difference());
            document.add(summary);

            if (type == ReportType.DETAILED) {
                document.add(new Paragraph(ReportMessages.text(
                        "finance.treasury.cash_register.reports.movements"))
                        .setFont(bold).setFontSize(14));
                Table details = new Table(UnitValue.createPercentArray(
                        new float[]{1.4f, 1.1f, 1.2f, 1.2f, 1.2f, 2.4f, 1.5f}))
                        .useAllAvailableWidth();
                for (String title : new String[]{
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.date"),
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.direction"),
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.type"),
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.method"),
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.amount"),
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.description"),
                        ReportMessages.text(
                                "finance.treasury.cash_register.reports.user")}) {
                    details.addHeaderCell(
                            new Cell().add(new Paragraph(title).setFont(bold)));
                }
                for (CashMovementResponse movement : register.movements()) {
                    details.addCell(format(movement.occurredAt()));
                    details.addCell(
                            ReportMessages.code(movement.direction().name()));
                    details.addCell(
                            ReportMessages.code(movement.type().name()));
                    details.addCell(value(movement.paymentMethodName()));
                    details.addCell(money(movement.amount()));
                    details.addCell(value(movementDescription(movement)));
                    details.addCell(value(movement.createdByName()));
                }
                document.add(details);
            }
            document.close();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate cash register PDF report", exception);
        }
    }

    private void addMovementSheet(XSSFWorkbook workbook,
            CashRegisterResponse register, CellStyle header) {
        var sheet = workbook.createSheet(ReportMessages
                .text("finance.treasury.cash_register.reports.details"));
        Row titles = sheet.createRow(0);
        String[] columns = {
                ReportMessages
                        .text("finance.treasury.cash_register.reports.date"),
                ReportMessages.text(
                        "finance.treasury.cash_register.reports.direction"),
                ReportMessages
                        .text("finance.treasury.cash_register.reports.type"),
                ReportMessages
                        .text("finance.treasury.cash_register.reports.method"),
                ReportMessages
                        .text("finance.treasury.cash_register.reports.amount"),
                ReportMessages.text(
                        "finance.treasury.cash_register.reports.description"),
                ReportMessages
                        .text("finance.treasury.cash_register.reports.source"),
                ReportMessages
                        .text("finance.treasury.cash_register.reports.user")};
        for (int i = 0; i < columns.length; i++) {
            titles.createCell(i).setCellValue(columns[i]);
            titles.getCell(i).setCellStyle(header);
        }
        int rowIndex = 1;
        for (CashMovementResponse movement : register.movements()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(format(movement.occurredAt()));
            row.createCell(1).setCellValue(
                    ReportMessages.code(movement.direction().name()));
            row.createCell(2)
                    .setCellValue(ReportMessages.code(movement.type().name()));
            row.createCell(3).setCellValue(value(movement.paymentMethodName()));
            row.createCell(4).setCellValue(movement.amount().doubleValue());
            row.createCell(5).setCellValue(value(movementDescription(movement)));
            row.createCell(6).setCellValue(value(movement.sourceType()));
            row.createCell(7).setCellValue(value(movement.createdByName()));
        }
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String movementDescription(CashMovementResponse movement) {
        String description = value(movement.description());
        if (!movement.cancelled()) {
            return description;
        }
        String suffix = ReportMessages
                .text("finance.treasury.cash_register.reports.cancelled");
        return description.isBlank() ? suffix : description + " (" + suffix + ")";
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

    private void addSummaryRow(Row row, String label, String value,
            CellStyle style) {
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
        return dateTime == null
                ? "—"
                : ReportMessages.dateTimeFormatter().format(dateTime);
    }

    private String money(BigDecimal amount) {
        return orZero(amount).setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }

    private BigDecimal orZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String value(String text) {
        return text == null ? "—" : text;
    }
}
