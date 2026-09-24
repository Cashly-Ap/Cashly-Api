package com.parsun.parsun_api.reports.orders.infrastructure.pdf;

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
import com.itextpdf.layout.properties.VerticalAlignment;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.reports.orders.application.port.OrderPdfPort;
import com.parsun.parsun_api.reports.infrastructure.pdf.PdfBranding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrderPdfGenerator implements OrderPdfPort {

    private static final DeviceRgb LIGHT_GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_GREEN = new DeviceRgb(34, 139, 34);
    private static final DeviceRgb COLOR_RED = new DeviceRgb(220, 20, 60);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 200);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generatePdf(OrderDetailResponse order) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);

            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 50, 40);

            PdfFont bold = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageFooterHandler(regular));

            document.add(buildHeader(order, bold, regular));
            document.add(buildSectionTitle("DATOS DEL CLIENTE", bold));
            document.add(buildClientTable(order, bold, regular));
            document.add(buildSectionTitle("DETALLE DE LA FACTURA", bold));
            document.add(buildLinesTable(order, bold, regular));
            document.add(buildTotalsTable(order, bold, regular));

            if (order.notes() != null && !order.notes().isEmpty()) {
                document.add(buildSectionTitle("OBSERVACIONES", bold));
                document.add(new Paragraph(order.notes()).setFont(regular).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY));
            }

            if (order.status().equals("CANCELLED") && order.cancelledReason() != null) {
                document.add(buildSectionTitle("MOTIVO DE CANCELACIÓN", bold));
                document.add(new Paragraph(order.cancelledReason()).setFont(regular).setFontSize(10).setFontColor(COLOR_RED));
            }

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating order PDF", e);
        }
        return baos.toByteArray();
    }

    private Table buildHeader(OrderDetailResponse order, PdfFont bold, PdfFont regular) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        header.setMarginBottom(20);

        Cell logoCell = new Cell()
                .add(PdfBranding.logo())
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(logoCell);

        String orderDate = order.createdAt().format(DATE_FORMATTER);
        Cell infoCell = new Cell()
                .add(new Paragraph("Nota N: " + order.orderNumber()).setFont(bold).setFontSize(11).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(2))
                .add(new Paragraph("Fecha: " + orderDate).setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.RIGHT).setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(infoCell);

        return header;
    }

    private Paragraph buildSectionTitle(String title, PdfFont bold) {
        return new Paragraph(title)
                .setFont(bold)
                .setFontSize(11)
                .setMarginTop(16)
                .setMarginBottom(6)
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1));
    }

    private Table buildClientTable(OrderDetailResponse order, PdfFont bold, PdfFont regular) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        table.setMarginBottom(8);

        table.addCell(buildClientCell("Cliente: ", order.customerName(), bold, regular, true));
        table.addCell(buildClientCell("RIF: ", order.customerRif() != null ? order.customerRif() : "-", bold, regular, false));

        return table;
    }

    private Cell buildClientCell(String label, String value, PdfFont bold, PdfFont regular, boolean leftBorder) {
        Paragraph p = new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label).setFont(bold).setFontSize(10))
                .add(new com.itextpdf.layout.element.Text(value).setFont(regular).setFontSize(10));

        Cell cell = new Cell()
                .add(p)
                .setBackgroundColor(LIGHT_GRAY_ROW)
                .setPadding(8)
                .setBorder(new SolidBorder(BORDER_COLOR, 1));

        return cell;
    }

    private Table buildLinesTable(OrderDetailResponse order, PdfFont bold, PdfFont regular) {
        float[] columnWidths = {2f, 4f, 2f, 2f, 2f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
        table.setMarginBottom(8);

        String[] headers = {"Codigo", "Descripcion", "Cantidad", "Precio Unit.", "Total"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(6)
                    .setBorder(Border.NO_BORDER));
        }

        for (int i = 0; i < order.lines().size(); i++) {
            var line = order.lines().get(i);
            DeviceRgb rowBg = (i % 2 != 0) ? LIGHT_GRAY_ROW : null;

            addTableCell(table, line.productCode() != null ? line.productCode() : "-", regular, rowBg, TextAlignment.CENTER);
            addTableCell(table, line.productName(), regular, rowBg, TextAlignment.LEFT);
            addTableCell(table, formatDecimal(line.quantity()), regular, rowBg, TextAlignment.CENTER);
            addTableCell(table, "$ " + formatDecimal(line.unitPrice()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(line.lineTotal()), regular, rowBg, TextAlignment.RIGHT);
        }

        return table;
    }

    private Table buildTotalsTable(OrderDetailResponse order, PdfFont bold, PdfFont regular) {
        float[] widths = {70f, 30f};
        Table table = new Table(UnitValue.createPercentArray(widths));
        table.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
        table.setWidth(UnitValue.createPercentValue(45));
        table.setMarginTop(8);
        table.setBorder(new SolidBorder(BORDER_COLOR, 1));

        addTotalsRow(table, "SUBTOTAL:", "$ " + formatDecimal(order.subtotal()), bold, regular, null);
        addTotalsRow(table, "TOTAL PAGADO:", "$ 0,00", bold, regular, COLOR_GREEN);
        addTotalsRow(table, "SALDO PENDIENTE:", "$ " + formatDecimal(order.subtotal()), bold, regular, COLOR_RED);

        return table;
    }

    private void addTotalsRow(Table table, String label, String value, PdfFont bold, PdfFont regular, DeviceRgb valueColor) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(9))
                .setPadding(6)
                .setBorder(Border.NO_BORDER));

        Paragraph valueParagraph = new Paragraph(value).setFont(bold).setFontSize(9).setTextAlignment(TextAlignment.RIGHT);
        if (valueColor != null) {
            valueParagraph.setFontColor(valueColor);
        }
        table.addCell(new Cell()
                .add(valueParagraph)
                .setPadding(6)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT));
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void addTableCell(Table table, String text, PdfFont font, DeviceRgb bg, TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(9))
                .setTextAlignment(align)
                .setPaddingTop(5)
                .setPaddingBottom(5)
                .setPaddingLeft(6)
                .setPaddingRight(6)
                .setBorder(Border.NO_BORDER);
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
