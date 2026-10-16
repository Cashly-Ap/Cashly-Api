package com.cashly.cashly_api.quotes.reporting.infrastructure.pdf;

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
import com.cashly.cashly_api.quotes.reporting.application.dto.model.QuoteReportRow;
import com.cashly.cashly_api.quotes.reporting.application.ports.out.QuotesReportPdfPort;
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

            PdfFont bold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterHandler(regular));

            Paragraph title = new Paragraph("Reporte de Presupuestos")
                    .setFont(bold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(4);

            document.add(title);

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String modeLabel = detailed ? "Detallado" : "Simple";
            Paragraph subtitle = new Paragraph("Generado el " + fecha + " | Modo: " + modeLabel + " | Total: " + quotes.size() + " presupuestos")
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(16);

            document.add(subtitle);

            float[] columnWidths = detailed
                    ? new float[]{3f, 3f, 2f, 2f, 2f}
                    : new float[]{3f, 3f, 2f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            String[] headers = detailed
                    ? new String[]{"N° Presupuesto", "Cliente", "Estado", "Total", "Fecha"}
                    : new String[]{"N° Presupuesto", "Cliente", "Total", "Fecha"};

            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6);
                table.addHeaderCell(headerCell);
            }

            for (int i = 0; i < quotes.size(); i++) {
                QuoteReportRow quote = quotes.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, quote.quoteNumber(), regular, rowColor, TextAlignment.LEFT);
                addCell(table, quote.customerName(), regular, rowColor, TextAlignment.LEFT);

                if (detailed) {
                    addStatusCell(table, formatStatus(quote.status()), regular, rowColor, TextAlignment.CENTER, quote.status());
                }

                addCell(table, "$" + formatDecimal(quote.subtotal()), regular, rowColor, TextAlignment.RIGHT);
                addCell(table, quote.createdAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), regular, rowColor, TextAlignment.CENTER);
            }

            document.add(table);

            Paragraph totalLabel = new Paragraph("Total de Presupuestos: " + quotes.size())
                    .setFont(bold)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(16);
            document.add(totalLabel);

            long draft = quotes.stream().filter(q -> "DRAFT".equals(q.status())).count();
            long convertedToOrder = quotes.stream().filter(q -> "CONVERTED_TO_ORDER".equals(q.status())).count();
            long convertedToInvoice = quotes.stream().filter(q -> "CONVERTED_TO_INVOICE".equals(q.status())).count();
            long cancelled = quotes.stream().filter(q -> "CANCELLED".equals(q.status())).count();

            Paragraph summary = new Paragraph(String.format("Borrador: %d | Convertidos a Pedido: %d | Convertidos a Factura: %d | Cancelados: %d",
                    draft, convertedToOrder, convertedToInvoice, cancelled))
                    .setFont(regular)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(8);
            document.add(summary);

            Paragraph footer = new Paragraph("Documento generado automaticamente por CASHLY")
                    .setFont(regular)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating quotes report PDF", e);
        }
        return baos.toByteArray();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "DRAFT" -> "Borrador";
            case "CONVERTED_TO_ORDER" -> "Pedido";
            case "CONVERTED_TO_INVOICE" -> "Factura";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
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
                        new Paragraph("Pagina " + pageNumber + " de " + totalPages)
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
