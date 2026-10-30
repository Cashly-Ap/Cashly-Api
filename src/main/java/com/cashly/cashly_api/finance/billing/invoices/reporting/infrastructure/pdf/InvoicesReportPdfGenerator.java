package com.cashly.cashly_api.finance.billing.invoices.reporting.infrastructure.pdf;

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
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportRow;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfStatusColors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InvoicesReportPdfGenerator implements InvoicesReportPdfPort {

    private final PdfBranding branding;

    public InvoicesReportPdfGenerator() { this(new PdfBranding()); }

    public InvoicesReportPdfGenerator(PdfBranding branding) { this.branding = branding; }

    private static final DeviceRgb GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    @Override
    public byte[] generatePdf(List<InvoiceReportRow> invoices, String mode) {
        boolean detailed = "detailed".equalsIgnoreCase(mode);

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

            Paragraph title = new Paragraph(ReportMessages.text("finance.billing.invoices.reports.invoicesReport"))
                    .setFont(bold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(4);

            document.add(branding.logo().setMarginBottom(12));
            document.add(title);

            String fecha = LocalDate.now().format(ReportMessages.dateFormatter());
            String modeLabel = detailed ? ReportMessages.text("finance.billing.invoices.reports.detailed") : ReportMessages.text("finance.billing.invoices.reports.simple");
            Paragraph subtitle = new Paragraph(ReportMessages.text("finance.billing.invoices.reports.generatedOn") + fecha + ReportMessages.text("finance.billing.invoices.reports.mode") + modeLabel + ReportMessages.text("finance.billing.invoices.reports.total") + invoices.size() + ReportMessages.text("finance.billing.invoices.reports.invoices"))
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(16);

            document.add(subtitle);

            float[] columnWidths = detailed
                    ? new float[]{2f, 3f, 1.5f, 1.5f, 1.5f, 2f, 1.5f}
                    : new float[]{2f, 3f, 2f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            String[] headers = detailed
                    ? new String[]{ReportMessages.text("finance.billing.invoices.reports.invoiceNo"), ReportMessages.text("finance.billing.invoices.reports.customer"), ReportMessages.text("finance.billing.invoices.reports.paymentType"), ReportMessages.text("finance.billing.invoices.reports.lines"), ReportMessages.text("finance.billing.invoices.reports.status"), ReportMessages.text("finance.billing.invoices.reports.totalLabel"), ReportMessages.text("finance.billing.invoices.reports.date")}
                    : new String[]{ReportMessages.text("finance.billing.invoices.reports.invoiceNo"), ReportMessages.text("finance.billing.invoices.reports.customer"), ReportMessages.text("finance.billing.invoices.reports.totalLabel"), ReportMessages.text("finance.billing.invoices.reports.date")};

            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6);
                table.addHeaderCell(headerCell);
            }

            for (int i = 0; i < invoices.size(); i++) {
                InvoiceReportRow invoice = invoices.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, invoice.invoiceNumber(), regular, rowColor, TextAlignment.LEFT);
                addCell(table, invoice.customerName(), regular, rowColor, TextAlignment.LEFT);

                if (detailed) {
                    addCell(table, invoice.paymentType() != null ? formatPaymentType(invoice.paymentType()) : "-", regular, rowColor, TextAlignment.CENTER);
                    addCell(table, String.valueOf(invoice.lineCount()), regular, rowColor, TextAlignment.CENTER);
                    addStatusCell(table, formatStatus(invoice.status()), regular, rowColor, TextAlignment.CENTER, invoice.status());
                }

                addCell(table, "$" + formatDecimal(invoice.total()), regular, rowColor, TextAlignment.RIGHT);
                addCell(table, invoice.createdAt().format(ReportMessages.dateTimeFormatter()), regular, rowColor, TextAlignment.CENTER);
            }

            document.add(table);

            Paragraph totalLabel = new Paragraph(ReportMessages.text("finance.billing.invoices.reports.totalInvoices") + invoices.size())
                    .setFont(bold)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(16);
            document.add(totalLabel);

            long paid = invoices.stream().filter(o -> "PAID".equals(o.status())).count();
            long pending = invoices.stream().filter(o -> "PENDING_PAYMENT".equals(o.status()) || "PARTIALLY_PAID".equals(o.status())).count();
            long cancelled = invoices.stream().filter(o -> "CANCELLED".equals(o.status())).count();
            long draft = invoices.stream().filter(o -> "DRAFT".equals(o.status())).count();

            Paragraph summary = new Paragraph(String.format(ReportMessages.text("finance.billing.invoices.reports.paidDPendingDCancelledDDraftD"),
                    paid, pending, cancelled, draft))
                    .setFont(regular)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(8);
            document.add(summary);

            Paragraph footer = new Paragraph(ReportMessages.text("finance.billing.invoices.reports.documentAutomaticallyGeneratedByCashly"))
                    .setFont(regular)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating invoices report PDF", e);
        }
        return baos.toByteArray();
    }

    private static String formatPaymentType(String paymentType) {
        if (paymentType == null) return "-";
        return switch (paymentType.toUpperCase()) {
            case "CASH" -> ReportMessages.text("finance.billing.invoices.reports.cash");
            case "CREDIT" -> ReportMessages.text("finance.billing.invoices.reports.credit");
            default -> paymentType;
        };
    }

    private static String formatStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "PAID" -> ReportMessages.text("finance.billing.invoices.reports.paid");
            case "PENDING_PAYMENT" -> ReportMessages.text("finance.billing.invoices.reports.pendingPayment");
            case "PARTIALLY_PAID" -> ReportMessages.text("finance.billing.invoices.reports.partiallyPaid");
            case "CANCELLED" -> ReportMessages.text("finance.billing.invoices.reports.cancelled");
            case "OVERDUE" -> ReportMessages.text("finance.billing.invoices.reports.overdue");
            default -> status;
        };
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }

    private void addCell(Table table, String text, PdfFont font, DeviceRgb bg, TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(font).setFontSize(8))
                .setTextAlignment(align)
                .setPaddingTop(4)
                .setPaddingBottom(4)
                .setPaddingLeft(5)
                .setPaddingRight(5);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        table.addCell(cell);
    }

    private void addStatusCell(Table table, String text, PdfFont font, DeviceRgb bg, TextAlignment align, String status) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "-")
                        .setFont(font)
                        .setFontSize(8)
                        .setFontColor(PdfStatusColors.forStatus(status)))
                .setTextAlignment(align)
                .setPaddingTop(4)
                .setPaddingBottom(4)
                .setPaddingLeft(5)
                .setPaddingRight(5);
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

            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
            com.itextpdf.kernel.geom.Rectangle pageSize = page.getPageSize();

            try (Canvas layoutCanvas = new Canvas(canvas, pageSize)) {
                layoutCanvas.showTextAligned(
                        new Paragraph(ReportMessages.text("finance.billing.invoices.reports.page") + pageNumber + ReportMessages.text("finance.billing.invoices.reports.of") + totalPages)
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
