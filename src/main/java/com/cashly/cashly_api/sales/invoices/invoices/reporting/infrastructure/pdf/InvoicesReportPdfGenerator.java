package com.cashly.cashly_api.sales.invoices.invoices.reporting.infrastructure.pdf;

import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.dto.model.InvoiceReportRow;
import com.cashly.cashly_api.sales.invoices.invoices.reporting.application.ports.out.InvoicesReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfStatusColors;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoicesReportPdfGenerator implements InvoicesReportPdfPort {

    private static final DeviceRgb GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final float MARGIN = 40f;
    private static final float ROW_HEIGHT = 15f;
    private static final float HEADER_HEIGHT = 18f;
    private static final float FONT_SIZE = 8f;

    private final PdfBranding branding;

    public InvoicesReportPdfGenerator() {
        this(new PdfBranding());
    }

    public InvoicesReportPdfGenerator(PdfBranding branding) {
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<InvoiceReportRow> invoices, String mode) {
        boolean detailed = "detailed".equalsIgnoreCase(mode);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            PageSize size = PageSize.A4.rotate();
            PdfFont bold = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
            float contentWidth = size.getWidth() - 2 * MARGIN;
            float[] widths = widths(detailed);
            scaleWidths(widths, contentWidth);
            String[] headers = headers(detailed);
            TextAlignment[] alignments = alignments(detailed);

            float firstTableTop = size.getHeight() - MARGIN - 120f;
            float nextTableTop = size.getHeight() - MARGIN - 10f;
            int firstRows = rowsThatFit(firstTableTop, size.getHeight());
            int nextRows = rowsThatFit(nextTableTop, size.getHeight());
            int pages = pages(invoices.size(), firstRows, nextRows);

            int index = 0;
            for (int pageNumber = 1; pageNumber <= pages; pageNumber++) {
                PdfPage page = pdf.addNewPage(size);
                PdfCanvas canvas = new PdfCanvas(page);
                boolean first = pageNumber == 1;
                float tableTop = first ? firstTableTop : nextTableTop;
                if (first) {
                    drawLogo(canvas, size);
                    drawText(canvas, ReportMessages.text(
                            "sales.invoices.reports.invoicesReport"), bold, 18f,
                            ColorConstants.BLACK, MARGIN,
                            size.getHeight() - MARGIN - 70f, contentWidth,
                            TextAlignment.LEFT);
                    drawText(canvas, subtitle(detailed, invoices.size()),
                            regular, 9f, ColorConstants.DARK_GRAY, MARGIN,
                            size.getHeight() - MARGIN - 88f, contentWidth,
                            TextAlignment.LEFT);
                }
                drawTableHeader(canvas, headers, widths, tableTop, bold);
                float y = tableTop - HEADER_HEIGHT - ROW_HEIGHT + 4f;
                int capacity = first ? firstRows : nextRows;
                for (int row = 0; row < capacity && index < invoices.size();
                        row++, index++) {
                    if (row % 2 != 0) {
                        fillRow(canvas, y, contentWidth);
                    }
                    drawRow(canvas, invoices.get(index), detailed, widths,
                            alignments, y, regular);
                    drawLine(canvas, y - 4f, contentWidth);
                    y -= ROW_HEIGHT;
                }
                drawFooter(canvas, size, pageNumber, pages, regular);
                if (pageNumber == 1 && pages == 0) {
                    drawSummary(canvas, invoices, regular, bold, size);
                } else if (index >= invoices.size()) {
                    drawSummary(canvas, invoices, regular, bold, size);
                }
            }
            pdf.close();
        } catch (IOException exception) {
            throw new RuntimeException("Error generating invoices report PDF",
                    exception);
        }
        return output.toByteArray();
    }

    private static int rowsThatFit(float tableTop, float pageHeight) {
        float available = tableTop - HEADER_HEIGHT - (MARGIN + 40f);
        return Math.max(1, (int) (available / ROW_HEIGHT));
    }

    private static int pages(int rows, int firstRows, int nextRows) {
        if (rows <= 0) {
            return 1;
        }
        if (rows <= firstRows) {
            return 1;
        }
        return 1 + (rows - firstRows + nextRows - 1) / nextRows;
    }

    private static String subtitle(boolean detailed, int total) {
        String modeLabel = detailed
                ? ReportMessages.text("sales.invoices.reports.detailed")
                : ReportMessages.text("sales.invoices.reports.simple");
        return ReportMessages.text("sales.invoices.reports.generatedOn")
                + LocalDate.now().format(ReportMessages.dateFormatter())
                + ReportMessages.text("sales.invoices.reports.mode") + modeLabel
                + ReportMessages.text("sales.invoices.reports.total") + total
                + ReportMessages.text("sales.invoices.reports.invoices");
    }

    private void drawLogo(PdfCanvas canvas, PageSize size) {
        byte[] bytes = branding.logoBytes();
        if (bytes == null) {
            return;
        }
        ImageData image = ImageDataFactory.create(bytes);
        float height = 45f;
        float width = Math.min(125f, image.getWidth() * height
                / Math.max(1f, image.getHeight()));
        canvas.addImageFittedIntoRectangle(image, new Rectangle(MARGIN,
                size.getHeight() - MARGIN - height, width, height), false);
    }

    private static String[] headers(boolean detailed) {
        if (detailed) {
            return new String[]{
                    ReportMessages.text("sales.invoices.reports.invoiceNo"),
                    ReportMessages.text("sales.invoices.reports.customer"),
                    ReportMessages.text("sales.invoices.reports.paymentType"),
                    ReportMessages.text("sales.invoices.reports.lines"),
                    ReportMessages.text("sales.invoices.reports.status"),
                    ReportMessages.text("sales.invoices.reports.totalLabel"),
                    ReportMessages.text("sales.invoices.reports.date")};
        }
        return new String[]{
                ReportMessages.text("sales.invoices.reports.invoiceNo"),
                ReportMessages.text("sales.invoices.reports.customer"),
                ReportMessages.text("sales.invoices.reports.totalLabel"),
                ReportMessages.text("sales.invoices.reports.date")};
    }

    private static TextAlignment[] alignments(boolean detailed) {
        return detailed
                ? new TextAlignment[]{TextAlignment.LEFT, TextAlignment.LEFT,
                        TextAlignment.CENTER, TextAlignment.CENTER,
                        TextAlignment.CENTER, TextAlignment.RIGHT,
                        TextAlignment.CENTER}
                : new TextAlignment[]{TextAlignment.LEFT, TextAlignment.LEFT,
                        TextAlignment.RIGHT, TextAlignment.CENTER};
    }

    private static float[] widths(boolean detailed) {
        return detailed
                ? new float[]{2f, 3f, 1.5f, 1.5f, 1.5f, 2f, 1.5f}
                : new float[]{2f, 3f, 2f, 2f};
    }

    private static void scaleWidths(float[] weights, float total) {
        float sum = 0f;
        for (float weight : weights) {
            sum += weight;
        }
        for (int i = 0; i < weights.length; i++) {
            weights[i] = weights[i] / sum * total;
        }
    }

    private static void drawTableHeader(PdfCanvas canvas, String[] headers,
            float[] widths, float top, PdfFont bold) {
        canvas.setFillColor(HEADER_BG);
        float total = 0f;
        for (float width : widths) {
            total += width;
        }
        canvas.rectangle(MARGIN, top - HEADER_HEIGHT, total, HEADER_HEIGHT)
                .fill();
        float x = MARGIN + 4f;
        for (int i = 0; i < headers.length; i++) {
            drawText(canvas, headers[i], bold, 8f, HEADER_TEXT, x,
                    top - HEADER_HEIGHT + 5f, widths[i] - 8f,
                    TextAlignment.CENTER);
            x += widths[i];
        }
    }

    private static void drawRow(PdfCanvas canvas, InvoiceReportRow invoice,
            boolean detailed, float[] widths, TextAlignment[] alignments,
            float y, PdfFont regular) {
        String[] values = detailed
                ? new String[]{invoice.invoiceNumber(), invoice.customerName(),
                        formatPaymentType(invoice.paymentType()),
                        String.valueOf(invoice.lineCount()),
                        formatStatus(invoice.status()),
                        "$" + formatDecimal(invoice.total()),
                        invoice.createdAt()
                                .format(ReportMessages.dateTimeFormatter())}
                : new String[]{invoice.invoiceNumber(), invoice.customerName(),
                        "$" + formatDecimal(invoice.total()),
                        invoice.createdAt()
                                .format(ReportMessages.dateTimeFormatter())};
        float x = MARGIN + 4f;
        for (int i = 0; i < values.length; i++) {
            Color color = i < alignments.length
                    && "status".equals(columnKind(detailed, i))
                            ? PdfStatusColors.forStatus(invoice.status())
                            : ColorConstants.BLACK;
            drawText(canvas, values[i], regular, FONT_SIZE, color, x, y,
                    widths[i] - 8f, alignments[i]);
            x += widths[i];
        }
    }

    private static String columnKind(boolean detailed, int index) {
        return detailed && index == 4 ? "status" : "text";
    }

    private static void fillRow(PdfCanvas canvas, float y, float width) {
        canvas.setFillColor(GRAY_ROW);
        canvas.rectangle(MARGIN, y - 4f, width, ROW_HEIGHT).fill();
    }

    private static void drawLine(PdfCanvas canvas, float y, float width) {
        canvas.setStrokeColor(new DeviceRgb(220, 220, 220))
                .setLineWidth(0.3f).moveTo(MARGIN, y).lineTo(MARGIN + width, y)
                .stroke();
    }

    private static void drawText(PdfCanvas canvas, String text, PdfFont font,
            float size, Color color, float x, float y, float maxWidth,
            TextAlignment alignment) {
        PdfFont actual = font;
        String value = text == null ? "" : text;
        value = truncate(value, actual, size, Math.max(4f, maxWidth));
        float drawX = x;
        if (alignment == TextAlignment.RIGHT) {
            drawX = x + maxWidth - actual.getWidth(value, size);
        } else if (alignment == TextAlignment.CENTER) {
            drawX = x + (maxWidth - actual.getWidth(value, size)) / 2f;
        }
        canvas.setFillColor(color == null ? ColorConstants.BLACK : color);
        canvas.beginText().setFontAndSize(actual, size).moveText(drawX, y)
                .showText(value).endText();
    }

    private static String truncate(String value, PdfFont font, float size,
            float maxWidth) {
        if (font.getWidth(value, size) <= maxWidth) {
            return value;
        }
        String ellipsis = "…";
        int length = value.length();
        while (length > 0 && font.getWidth(
                value.substring(0, length) + ellipsis, size) > maxWidth) {
            length--;
        }
        return value.substring(0, length) + ellipsis;
    }

    private static void drawFooter(PdfCanvas canvas, PageSize size,
            int pageNumber, int totalPages, PdfFont regular) {
        drawText(canvas, ReportMessages.text("sales.invoices.reports.page")
                + pageNumber + ReportMessages.text("sales.invoices.reports.of")
                + totalPages, regular, 8f, ColorConstants.GRAY, MARGIN,
                MARGIN - 20f, size.getWidth() - 2 * MARGIN,
                TextAlignment.CENTER);
    }

    private static void drawSummary(PdfCanvas canvas,
            List<InvoiceReportRow> invoices, PdfFont regular, PdfFont bold,
            PageSize size) {
        long paid = invoices.stream().filter(i -> "PAID".equals(i.status()))
                .count();
        long pending = invoices.stream()
                .filter(i -> "PENDING_PAYMENT".equals(i.status())
                        || "PARTIALLY_PAID".equals(i.status()))
                .count();
        long cancelled = invoices.stream()
                .filter(i -> "CANCELLED".equals(i.status())).count();
        long draft = invoices.stream().filter(i -> "DRAFT".equals(i.status()))
                .count();
        String summary = String.format(ReportMessages.text(
                "sales.invoices.reports.paidDPendingDCancelledDDraftD"), paid,
                pending, cancelled, draft);
        drawText(canvas, summary, regular, 9f, ColorConstants.DARK_GRAY, MARGIN,
                MARGIN + 5f, size.getWidth() - 2 * MARGIN,
                TextAlignment.RIGHT);
    }

    private static String formatPaymentType(String paymentType) {
        if (paymentType == null) {
            return "-";
        }
        return switch (paymentType.toUpperCase()) {
            case "CASH" -> ReportMessages.text("sales.invoices.reports.cash");
            case "CREDIT" -> ReportMessages.text("sales.invoices.reports.credit");
            default -> paymentType;
        };
    }

    private static String formatStatus(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case "PAID" -> ReportMessages.text("sales.invoices.reports.paid");
            case "PENDING_PAYMENT" -> ReportMessages
                    .text("sales.invoices.reports.pendingPayment");
            case "PARTIALLY_PAID" -> ReportMessages
                    .text("sales.invoices.reports.partiallyPaid");
            case "CANCELLED" -> ReportMessages
                    .text("sales.invoices.reports.cancelled");
            case "OVERDUE" -> ReportMessages
                    .text("sales.invoices.reports.overdue");
            default -> status;
        };
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(ReportMessages.locale(), "%.2f", value);
    }
}
