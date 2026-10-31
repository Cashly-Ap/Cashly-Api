package com.cashly.cashly_api.sales.quotes.reporting.infrastructure.pdf;

import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.cashly.cashly_api.sales.quotes.reporting.application.dto.model.QuoteReportRow;
import com.cashly.cashly_api.sales.quotes.reporting.application.ports.out.QuotesReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfStatusColors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class QuotesReportPdfGenerator implements QuotesReportPdfPort {

    private final PdfBranding branding;

    public QuotesReportPdfGenerator() {
        this(new PdfBranding());
    }

    public QuotesReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    private static final DeviceRgb GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    @Override
    public byte[] generatePdf(List<QuoteReportRow> quotes, String mode) {
        boolean detailed = "detailed".equalsIgnoreCase(mode);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4.rotate());

            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 50, 40);

            PdfFont bold = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE,
                    new PageFooterHandler(regular));

            Paragraph title = new Paragraph(
                    ReportMessages.text("sales.quotes.reports.quotesReport"))
                    .setFont(bold).setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT).setMarginBottom(4);

            document.add(branding.logo().setMarginBottom(12));
            document.add(title);

            String fecha = LocalDate.now()
                    .format(ReportMessages.dateFormatter());
            String modeLabel = detailed
                    ? ReportMessages.text("sales.quotes.reports.detailed")
                    : ReportMessages.text("sales.quotes.reports.simple");
            Paragraph subtitle = new Paragraph(
                    ReportMessages.text("sales.quotes.reports.generatedOn")
                            + fecha
                            + ReportMessages.text("sales.quotes.reports.mode")
                            + modeLabel
                            + ReportMessages
                                    .text("sales.quotes.reports.totalLabel")
                            + quotes.size()
                            + ReportMessages
                                    .text("sales.quotes.reports.quotes"))
                    .setFont(regular).setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT).setMarginBottom(16);

            document.add(subtitle);

            float[] columnWidths = detailed
                    ? new float[]{3f, 3f, 2f, 2f, 2f}
                    : new float[]{3f, 3f, 2f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            String[] headers = detailed
                    ? new String[]{
                            ReportMessages
                                    .text("sales.quotes.reports.quoteNoLabel"),
                            ReportMessages
                                    .text("sales.quotes.reports.customerLabel"),
                            ReportMessages.text("sales.quotes.reports.status"),
                            ReportMessages.text("sales.quotes.reports.total"),
                            ReportMessages
                                    .text("sales.quotes.reports.dateLabel")}
                    : new String[]{
                            ReportMessages
                                    .text("sales.quotes.reports.quoteNoLabel"),
                            ReportMessages
                                    .text("sales.quotes.reports.customerLabel"),
                            ReportMessages.text("sales.quotes.reports.total"),
                            ReportMessages
                                    .text("sales.quotes.reports.dateLabel")};

            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9)
                                .setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER).setPadding(6);
                table.addHeaderCell(headerCell);
            }

            for (int i = 0; i < quotes.size(); i++) {
                QuoteReportRow quote = quotes.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, quote.quoteNumber(), regular, rowColor,
                        TextAlignment.LEFT);
                addCell(table, quote.customerName(), regular, rowColor,
                        TextAlignment.LEFT);

                if (detailed) {
                    addStatusCell(table, formatStatus(quote.status()), regular,
                            rowColor, TextAlignment.CENTER, quote.status());
                }

                addCell(table, "$" + formatDecimal(quote.subtotal()), regular,
                        rowColor, TextAlignment.RIGHT);
                addCell(table,
                        quote.createdAt().atZone(ZoneId.systemDefault())
                                .format(ReportMessages.dateTimeFormatter()),
                        regular, rowColor, TextAlignment.CENTER);
            }

            document.add(table);

            Paragraph totalLabel = new Paragraph(
                    ReportMessages.text("sales.quotes.reports.totalQuotes")
                            + quotes.size())
                    .setFont(bold).setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(16);
            document.add(totalLabel);

            long draft = quotes.stream().filter(q -> "DRAFT".equals(q.status()))
                    .count();
            long convertedToOrder = quotes.stream()
                    .filter(q -> "CONVERTED_TO_ORDER".equals(q.status()))
                    .count();
            long convertedToInvoice = quotes.stream()
                    .filter(q -> "CONVERTED_TO_INVOICE".equals(q.status()))
                    .count();
            long cancelled = quotes.stream()
                    .filter(q -> "CANCELLED".equals(q.status())).count();

            Paragraph summary = new Paragraph(String.format(ReportMessages.text(
                    "sales.quotes.reports.draftDConvertedToOrderDConvertedToInvoiceDCancelledD"),
                    draft, convertedToOrder, convertedToInvoice, cancelled))
                    .setFont(regular).setFontSize(9)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(8);
            document.add(summary);

            Paragraph footer = new Paragraph(ReportMessages.text(
                    "sales.quotes.reports.documentAutomaticallyGeneratedByCashly"))
                    .setFont(regular).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30);
            document.add(footer);

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating quotes report PDF", e);
        }
        return baos.toByteArray();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }

    private String formatStatus(String status) {
        if (status == null)
            return "-";
        return switch (status) {
            case "DRAFT" -> ReportMessages.text("sales.quotes.reports.draft");
            case "CONVERTED_TO_ORDER" ->
                ReportMessages.text("sales.quotes.reports.order");
            case "CONVERTED_TO_INVOICE" ->
                ReportMessages.text("sales.quotes.reports.invoice");
            case "CANCELLED" ->
                ReportMessages.text("sales.quotes.reports.cancelled");
            default -> status;
        };
    }

    private void addCell(Table table, String text, PdfFont font, DeviceRgb bg,
            TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(font)
                        .setFontSize(8))
                .setTextAlignment(align).setPaddingTop(4).setPaddingBottom(4)
                .setPaddingLeft(5).setPaddingRight(5);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        table.addCell(cell);
    }

    private void addStatusCell(Table table, String text, PdfFont font,
            DeviceRgb bg, TextAlignment align, String status) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(font)
                        .setFontSize(8)
                        .setFontColor(PdfStatusColors.forStatus(status)))
                .setTextAlignment(align).setPaddingTop(4).setPaddingBottom(4)
                .setPaddingLeft(5).setPaddingRight(5);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        table.addCell(cell);
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

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(),
                    page.getResources(), pdfDoc);
            com.itextpdf.kernel.geom.Rectangle pageSize = page.getPageSize();

            try (Canvas layoutCanvas = new Canvas(canvas, pageSize)) {
                layoutCanvas.showTextAligned(
                        new Paragraph(
                                ReportMessages.text("sales.quotes.reports.page")
                                        + pageNumber
                                        + ReportMessages
                                                .text("sales.quotes.reports.of")
                                        + totalPages)
                                .setFont(font).setFontSize(8)
                                .setFontColor(ColorConstants.GRAY),
                        pageSize.getWidth() / 2, 20, TextAlignment.CENTER);
            }
        }
    }
}
