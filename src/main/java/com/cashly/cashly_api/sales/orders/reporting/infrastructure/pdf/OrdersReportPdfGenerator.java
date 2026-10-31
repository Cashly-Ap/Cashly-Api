package com.cashly.cashly_api.sales.orders.reporting.infrastructure.pdf;

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
import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportRow;
import com.cashly.cashly_api.sales.orders.reporting.application.ports.out.OrdersReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfStatusColors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OrdersReportPdfGenerator implements OrdersReportPdfPort {

    private final PdfBranding branding;

    public OrdersReportPdfGenerator() {
        this(new PdfBranding());
    }

    public OrdersReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    private static final DeviceRgb GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    @Override
    public byte[] generatePdf(List<OrderReportRow> orders, String mode) {
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
                    ReportMessages.text("sales.orders.reports.ordersReport"))
                    .setFont(bold).setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT).setMarginBottom(4);

            document.add(branding.logo().setMarginBottom(12));
            document.add(title);

            String fecha = LocalDate.now()
                    .format(ReportMessages.dateFormatter());
            String modeLabel = detailed
                    ? ReportMessages.text("sales.orders.reports.detailed")
                    : ReportMessages.text("sales.orders.reports.simple");
            Paragraph subtitle = new Paragraph(
                    ReportMessages.text("sales.orders.reports.generatedOn")
                            + fecha
                            + ReportMessages.text("sales.orders.reports.mode")
                            + modeLabel
                            + ReportMessages.text("sales.orders.reports.total")
                            + orders.size()
                            + ReportMessages
                                    .text("sales.orders.reports.orders"))
                    .setFont(regular).setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT).setMarginBottom(16);

            document.add(subtitle);

            float[] columnWidths = detailed
                    ? new float[]{2f, 3f, 1.5f, 1.5f, 2f, 2f, 1.5f}
                    : new float[]{2f, 3f, 2f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            String[] headers = detailed
                    ? new String[]{
                            ReportMessages.text("sales.orders.reports.orderNo"),
                            ReportMessages
                                    .text("sales.orders.reports.customer"),
                            ReportMessages.text("sales.orders.reports.lines"),
                            ReportMessages.text("sales.orders.reports.invoice"),
                            ReportMessages.text("sales.orders.reports.status"),
                            ReportMessages
                                    .text("sales.orders.reports.totalLabel"),
                            ReportMessages.text("sales.orders.reports.date")}
                    : new String[]{
                            ReportMessages.text("sales.orders.reports.orderNo"),
                            ReportMessages
                                    .text("sales.orders.reports.customer"),
                            ReportMessages
                                    .text("sales.orders.reports.totalLabel"),
                            ReportMessages.text("sales.orders.reports.date")};

            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9)
                                .setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER).setPadding(6);
                table.addHeaderCell(headerCell);
            }

            for (int i = 0; i < orders.size(); i++) {
                OrderReportRow order = orders.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, order.orderNumber(), regular, rowColor,
                        TextAlignment.LEFT);
                addCell(table, order.customerName(), regular, rowColor,
                        TextAlignment.LEFT);

                if (detailed) {
                    addCell(table, String.valueOf(order.lineCount()), regular,
                            rowColor, TextAlignment.CENTER);
                    addCell(table, order.invoiceId() != null
                            ? ReportMessages.text("sales.orders.reports.yes")
                            : "-", regular, rowColor, TextAlignment.CENTER);
                    addStatusCell(table, formatStatus(order.status()), regular,
                            rowColor, TextAlignment.CENTER, order.status());
                }

                addCell(table, "$" + formatDecimal(order.subtotal()), regular,
                        rowColor, TextAlignment.RIGHT);
                addCell(table,
                        order.createdAt()
                                .format(ReportMessages.dateTimeFormatter()),
                        regular, rowColor, TextAlignment.CENTER);
            }

            document.add(table);

            Paragraph totalLabel = new Paragraph(
                    ReportMessages.text("sales.orders.reports.totalOrders")
                            + orders.size())
                    .setFont(bold).setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(16);
            document.add(totalLabel);

            long confirmed = orders.stream()
                    .filter(o -> "CONFIRMED".equals(o.status())).count();
            long invoiced = orders.stream()
                    .filter(o -> "INVOICED".equals(o.status())).count();
            long cancelled = orders.stream()
                    .filter(o -> "CANCELLED".equals(o.status())).count();
            long draft = orders.stream().filter(o -> "DRAFT".equals(o.status()))
                    .count();

            Paragraph summary = new Paragraph(String.format(ReportMessages.text(
                    "sales.orders.reports.confirmedDInvoicedDCancelledDDraftD"),
                    confirmed, invoiced, cancelled, draft)).setFont(regular)
                    .setFontSize(9).setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(8);
            document.add(summary);

            Paragraph footer = new Paragraph(ReportMessages.text(
                    "sales.orders.reports.documentAutomaticallyGeneratedByCashly"))
                    .setFont(regular).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30);
            document.add(footer);

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating orders report PDF", e);
        }
        return baos.toByteArray();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }

    private static String formatStatus(String status) {
        if (status == null)
            return "-";
        return switch (status) {
            case "DRAFT" -> ReportMessages.text("sales.orders.reports.draft");
            case "CONFIRMED" ->
                ReportMessages.text("sales.orders.reports.confirmed");
            case "INVOICED" ->
                ReportMessages.text("sales.orders.reports.invoiced");
            case "IN_PREPARATION" ->
                ReportMessages.text("sales.orders.reports.inPreparation");
            case "READY_FOR_DISPATCH" ->
                ReportMessages.text("sales.orders.reports.readyForDispatch");
            case "DISPATCHED" ->
                ReportMessages.text("sales.orders.reports.dispatched");
            case "DELIVERED" ->
                ReportMessages.text("sales.orders.reports.delivered");
            case "CANCELLED" ->
                ReportMessages.text("sales.orders.reports.cancelled");
            default -> status;
        };
    }

    private TextAlignment getStatusAlignment(String status) {
        if (status == null)
            return TextAlignment.CENTER;
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> TextAlignment.CENTER;
            case "INVOICED" -> TextAlignment.CENTER;
            case "CANCELLED" -> TextAlignment.CENTER;
            default -> TextAlignment.CENTER;
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
                                ReportMessages.text("sales.orders.reports.page")
                                        + pageNumber
                                        + ReportMessages
                                                .text("sales.orders.reports.of")
                                        + totalPages)
                                .setFont(font).setFontSize(8)
                                .setFontColor(ColorConstants.GRAY),
                        pageSize.getWidth() / 2, 20, TextAlignment.CENTER);
            }
        }
    }
}
