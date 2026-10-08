package com.cashly.cashly_api.reports.orders.infrastructure.pdf;

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
import com.cashly.cashly_api.reports.orders.application.dto.OrderReportRow;
import com.cashly.cashly_api.reports.orders.application.port.OrdersReportPdfPort;
import com.cashly.cashly_api.reports.infrastructure.pdf.PdfStatusColors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OrdersReportPdfGenerator implements OrdersReportPdfPort {

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

            PdfFont bold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterHandler(regular));

            
            Paragraph title = new Paragraph("Reporte de Pedidos")
                    .setFont(bold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(4);

            document.add(title);

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String modeLabel = detailed ? "Detallado" : "Simple";
            Paragraph subtitle = new Paragraph("Generado el " + fecha + " | Modo: " + modeLabel + " | Total: " + orders.size() + " pedidos")
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(16);

            document.add(subtitle);

            
            float[] columnWidths = detailed
                    ? new float[]{2f, 3f, 1.5f, 1.5f, 2f, 2f, 1.5f}
                    : new float[]{2f, 3f, 2f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            
            String[] headers = detailed
                    ? new String[]{"N° Pedido", "Cliente", "Líneas", "Factura", "Estado", "Total", "Fecha"}
                    : new String[]{"N° Pedido", "Cliente", "Total", "Fecha"};

            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6);
                table.addHeaderCell(headerCell);
            }

            
            for (int i = 0; i < orders.size(); i++) {
                OrderReportRow order = orders.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, order.orderNumber(), regular, rowColor, TextAlignment.LEFT);
                addCell(table, order.customerName(), regular, rowColor, TextAlignment.LEFT);

                if (detailed) {
                    addCell(table, String.valueOf(order.lineCount()), regular, rowColor, TextAlignment.CENTER);
                    addCell(table, order.invoiceId() != null ? "Sí" : "-", regular, rowColor, TextAlignment.CENTER);
                    addStatusCell(table, formatStatus(order.status()), regular, rowColor, TextAlignment.CENTER, order.status());
                }

                addCell(table, "$" + formatDecimal(order.subtotal()), regular, rowColor, TextAlignment.RIGHT);
                addCell(table, order.createdAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), regular, rowColor, TextAlignment.CENTER);
            }

            document.add(table);

            
            Paragraph totalLabel = new Paragraph("Total de Pedidos: " + orders.size())
                    .setFont(bold)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(16);
            document.add(totalLabel);

            
            long confirmed = orders.stream().filter(o -> "CONFIRMED".equals(o.status())).count();
            long invoiced = orders.stream().filter(o -> "INVOICED".equals(o.status())).count();
            long cancelled = orders.stream().filter(o -> "CANCELLED".equals(o.status())).count();
            long draft = orders.stream().filter(o -> "DRAFT".equals(o.status())).count();

            Paragraph summary = new Paragraph(String.format("Confirmados: %d | Facturados: %d | Cancelados: %d | Borrador: %d",
                    confirmed, invoiced, cancelled, draft))
                    .setFont(regular)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(8);
            document.add(summary);

            
            Paragraph footer = new Paragraph("Documento generado automáticamente por CASHLY")
                    .setFont(regular)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating orders report PDF", e);
        }
        return baos.toByteArray();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String formatStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "DRAFT" -> "Borrador";
            case "CONFIRMED" -> "Confirmado";
            case "INVOICED" -> "Facturado";
            case "IN_PREPARATION" -> "En Preparación";
            case "READY_FOR_DISPATCH" -> "Listo para Despacho";
            case "DISPATCHED" -> "Despachado";
            case "DELIVERED" -> "Entregado";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }

    private TextAlignment getStatusAlignment(String status) {
        if (status == null) return TextAlignment.CENTER;
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> TextAlignment.CENTER;
            case "INVOICED" -> TextAlignment.CENTER;
            case "CANCELLED" -> TextAlignment.CENTER;
            default -> TextAlignment.CENTER;
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
