package com.parsun.parsun_api.reports.movements.infrastructure.pdf;

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
import com.parsun.parsun_api.reports.movements.application.dto.response.MovementReportResponse;
import com.parsun.parsun_api.reports.movements.application.port.MovementsPdfPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MovementsPdfGenerator implements MovementsPdfPort {

    private static final DeviceRgb GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);

    @Override
    public byte[] generatePdf(List<MovementReportResponse> items) {
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

            Paragraph title = new Paragraph("Historial de Movimientos")
                    .setFont(bold)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(4);

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph subtitle = new Paragraph("Generado el " + fecha)
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(16);

            document.add(title);
            document.add(subtitle);

            float[] columnWidths = {2f, 2.5f, 3f, 1.5f, 2f, 1.2f, 1.5f, 1.5f, 3f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths))
                    .useAllAvailableWidth();

            String[] headers = {"Fecha", "Almacén", "Producto", "SKU", "Tipo", "Cantidad", "Stock Anterior", "Stock Nuevo", "Notas"};
            for (String h : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                        .setBackgroundColor(HEADER_BG)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6);
                table.addHeaderCell(headerCell);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (int i = 0; i < items.size(); i++) {
                MovementReportResponse item = items.get(i);
                boolean even = i % 2 == 0;
                DeviceRgb rowColor = even ? null : GRAY_ROW;

                addCell(table, item.createdAt() != null ? item.createdAt().format(dtf) : "-", regular, rowColor, TextAlignment.CENTER);
                addCell(table, item.warehouseName(), regular, rowColor, TextAlignment.LEFT);
                addCell(table, item.productName(), regular, rowColor, TextAlignment.LEFT);
                addCell(table, item.sku(), regular, rowColor, TextAlignment.CENTER);
                addCell(table, item.movementType(), regular, rowColor, TextAlignment.CENTER);
                addCell(table, String.valueOf(item.quantity()), regular, rowColor, TextAlignment.RIGHT);
                addCell(table, String.valueOf(item.previousStock()), regular, rowColor, TextAlignment.RIGHT);
                addCell(table, String.valueOf(item.newStock()), regular, rowColor, TextAlignment.RIGHT);
                addCell(table, item.notes() != null ? item.notes() : "-", regular, rowColor, TextAlignment.LEFT);
            }

            document.add(table);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating movements PDF", e);
        }
        return baos.toByteArray();
    }

    private void addCell(Table table, String text, PdfFont font, DeviceRgb bg, TextAlignment align) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(8))
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
