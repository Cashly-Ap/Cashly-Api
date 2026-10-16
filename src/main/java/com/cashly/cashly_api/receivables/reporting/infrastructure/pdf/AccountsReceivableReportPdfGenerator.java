package com.cashly.cashly_api.receivables.reporting.infrastructure.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AccountsReceivableReportPdfGenerator implements AccountsReceivableReportPdfPort {

    private static final DeviceRgb LIGHT_GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 200);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generateReport(AccountsReceivableReportData data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());

            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 50, 40);

            PdfFont bold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterHandler(regular));

            document.add(buildTitle(bold));
            document.add(buildDate(data, regular));
            document.add(buildMainTable(data, bold, regular));
            document.add(buildSummary(data.summary(), bold, regular));

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating accounts receivable report PDF", e);
        }
        return baos.toByteArray();
    }

    private Paragraph buildTitle(PdfFont bold) {
        return new Paragraph("CUENTAS POR COBRAR")
                .setFont(bold)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
    }

    private Paragraph buildDate(AccountsReceivableReportData data, PdfFont regular) {
        return new Paragraph("Fecha: " + data.reportDate().format(DATE_FORMATTER))
                .setFont(regular)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
    }

    private Table buildMainTable(AccountsReceivableReportData data, PdfFont bold, PdfFont regular) {
        float[] columnWidths = {4f, 2.5f, 2.5f, 2.5f, 2.5f, 2f, 2f, 2f, 2f, 2f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        String[] headers = {"Cliente", "RIF", "Límite Crédito", "Saldo Pendiente", "Disponible", "0-30", "31-60", "61-90", "91-120", "120+"};
        for (String h : headers) {
            table.addHeaderCell(createHeaderCell(h, bold));
        }

        for (int i = 0; i < data.rows().size(); i++) {
            var row = data.rows().get(i);
            DeviceRgb rowBg = (i % 2 != 0) ? LIGHT_GRAY_ROW : null;

            addTableCell(table, row.customerName(), regular, rowBg, TextAlignment.LEFT);
            addTableCell(table, row.rif() != null ? row.rif() : "-", regular, rowBg, TextAlignment.CENTER);
            addTableCell(table, "$ " + formatDecimal(row.creditLimit()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.pendingBalance()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.available()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.age0_30()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.age31_60()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.age61_90()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.age91_120()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(row.age120Plus()), regular, rowBg, TextAlignment.RIGHT);
        }

        return table;
    }

    private Cell createHeaderCell(String text, PdfFont bold) {
        return new Cell()
                .add(new Paragraph(text).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                .setBackgroundColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
                .setBorder(Border.NO_BORDER);
    }

    private void addTableCell(Table table, String text, PdfFont font, DeviceRgb bg, TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(8))
                .setTextAlignment(align)
                .setPaddingTop(4)
                .setPaddingBottom(4)
                .setPaddingLeft(4)
                .setPaddingRight(4)
                .setBorder(Border.NO_BORDER);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        table.addCell(cell);
    }

    private Table buildSummary(AccountsReceivableReportData.Summary summary, PdfFont bold, PdfFont regular) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        table.setMarginTop(20);

        table.addCell(buildSummaryCell("Total Cuentas por Cobrar:", "$ " + formatDecimal(summary.totalReceivable()), bold, regular));
        table.addCell(buildSummaryCell("Por Vencer:", "$ " + formatDecimal(summary.notYetDue()), bold, regular));
        table.addCell(buildSummaryCell("Vencidas:", "$ " + formatDecimal(summary.overdue()), bold, regular));
        table.addCell(buildSummaryCell("Clientes con Crédito Excedido:", String.valueOf(summary.customersExceededCredit()), bold, regular));

        return table;
    }

    private Cell buildSummaryCell(String label, String value, PdfFont bold, PdfFont regular) {
        Paragraph p = new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label).setFont(bold).setFontSize(10))
                .add(new com.itextpdf.layout.element.Text(" " + value).setFont(regular).setFontSize(10));

        return new Cell()
                .add(p)
                .setPadding(6)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBackgroundColor(LIGHT_GRAY_ROW);
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static class PageFooterHandler implements IEventHandler {

        private final PdfFont font;

        PageFooterHandler(PdfFont font) {
            this.font = font;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);
            int totalPages = pdfDoc.getNumberOfPages();

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
            com.itextpdf.kernel.geom.Rectangle pageSize = page.getPageSize();

            try (Canvas layoutCanvas = new Canvas(canvas, pageSize)) {
                layoutCanvas.showTextAligned(
                        new Paragraph("Página " + pageNumber + " de " + totalPages)
                                .setFont(font)
                                .setFontSize(8)
                                .setFontColor(ColorConstants.GRAY),
                        pageSize.getWidth() / 2,
                        20,
                        TextAlignment.CENTER
                );
            }
        }
    }
}
