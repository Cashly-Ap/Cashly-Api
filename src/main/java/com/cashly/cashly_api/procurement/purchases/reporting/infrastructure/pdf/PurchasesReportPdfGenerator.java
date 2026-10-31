package com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.pdf;

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
import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportRow;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasesReportPdfPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PurchasesReportPdfGenerator implements PurchasesReportPdfPort {

    private final PdfBranding branding;

    public PurchasesReportPdfGenerator() {
        this(new PdfBranding());
    }

    public PurchasesReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    private static final DeviceRgb GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb STATUS_DRAFT = new DeviceRgb(128, 128, 128);
    private static final DeviceRgb STATUS_CONFIRMED = new DeviceRgb(34, 139,
            34);
    private static final DeviceRgb STATUS_CANCELLED = new DeviceRgb(220, 20,
            60);

    @Override
    public byte[] generatePdf(List<PurchaseReportRow> purchases, String mode) {
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

            Paragraph title = new Paragraph(ReportMessages
                    .text("procurement.purchases.reports.purchasesReport"))
                    .setFont(bold).setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT).setMarginBottom(4);

            document.add(branding.logo().setMarginBottom(12));
            document.add(title);

            String fecha = LocalDate.now()
                    .format(ReportMessages.dateFormatter());
            String modeLabel = detailed
                    ? ReportMessages
                            .text("procurement.purchases.reports.detailed")
                    : ReportMessages
                            .text("procurement.purchases.reports.simple");
            Paragraph subtitle = new Paragraph(ReportMessages
                    .text("procurement.purchases.reports.generatedOn") + fecha
                    + ReportMessages.text("procurement.purchases.reports.mode")
                    + modeLabel
                    + ReportMessages
                            .text("procurement.purchases.reports.totalLabel")
                    + purchases.size()
                    + ReportMessages
                            .text("procurement.purchases.reports.purchases"))
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
                    ? new String[]{ReportMessages.text(
                            "procurement.purchases.reports.purchaseNoLabel"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.supplierLabel"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.status"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.total"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.dateLabel")}
                    : new String[]{ReportMessages.text(
                            "procurement.purchases.reports.purchaseNoLabel"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.supplierLabel"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.total"),
                            ReportMessages.text(
                                    "procurement.purchases.reports.dateLabel")};

            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9)
                                .setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER).setPadding(6);
                table.addHeaderCell(headerCell);
            }

            for (int i = 0; i < purchases.size(); i++) {
                PurchaseReportRow purchase = purchases.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, purchase.purchaseNumber(), regular, rowColor,
                        TextAlignment.LEFT);
                addCell(table, purchase.supplierName(), regular, rowColor,
                        TextAlignment.LEFT);

                if (detailed) {
                    addCell(table, formatStatus(purchase.status()), regular,
                            rowColor, TextAlignment.CENTER);
                }

                addCell(table, "$" + formatDecimal(purchase.subtotal()),
                        regular, rowColor, TextAlignment.RIGHT);
                addCell(table,
                        purchase.createdAt().atZone(ZoneId.systemDefault())
                                .format(ReportMessages.dateTimeFormatter()),
                        regular, rowColor, TextAlignment.CENTER);
            }

            document.add(table);

            Paragraph totalLabel = new Paragraph(ReportMessages
                    .text("procurement.purchases.reports.totalPurchases")
                    + purchases.size()).setFont(bold).setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(16);
            document.add(totalLabel);

            long draft = purchases.stream()
                    .filter(q -> "DRAFT".equals(q.status())).count();
            long confirmed = purchases.stream()
                    .filter(q -> "CONFIRMED".equals(q.status())).count();
            long cancelled = purchases.stream()
                    .filter(q -> "CANCELLED".equals(q.status())).count();

            Paragraph summary = new Paragraph(String.format(ReportMessages.text(
                    "procurement.purchases.reports.draftDConfirmedDCancelledD"),
                    draft, confirmed, cancelled)).setFont(regular)
                    .setFontSize(9).setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT).setMarginTop(8);
            document.add(summary);

            Paragraph footer = new Paragraph(ReportMessages.text(
                    "procurement.purchases.reports.documentAutomaticallyGeneratedByCashly"))
                    .setFont(regular).setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30);
            document.add(footer);

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating purchases report PDF",
                    e);
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
            case "DRAFT" ->
                ReportMessages.text("procurement.purchases.reports.draft");
            case "CONFIRMED" ->
                ReportMessages.text("procurement.purchases.reports.confirmed");
            case "CANCELLED" ->
                ReportMessages.text("procurement.purchases.reports.cancelled");
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
                        new Paragraph(ReportMessages
                                .text("procurement.purchases.reports.page")
                                + pageNumber
                                + ReportMessages.text(
                                        "procurement.purchases.reports.of")
                                + totalPages).setFont(font).setFontSize(8)
                                .setFontColor(ColorConstants.GRAY),
                        pageSize.getWidth() / 2, 20, TextAlignment.CENTER);
            }
        }
    }
}
