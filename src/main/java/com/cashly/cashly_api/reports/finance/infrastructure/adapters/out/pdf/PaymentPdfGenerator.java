package com.cashly.cashly_api.reports.finance.infrastructure.adapters.out.pdf;

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
import com.cashly.cashly_api.reports.finance.application.dto.PaymentReportData;
import com.cashly.cashly_api.reports.finance.application.ports.out.PaymentPdfPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PaymentPdfGenerator implements PaymentPdfPort {

    private static final DeviceRgb LIGHT_GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 200);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generatePaymentPdf(PaymentReportData payment) {
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

            document.add(buildHeader(payment, bold, regular));
            document.add(buildSectionTitle("DATOS DEL PAGO", bold));
            document.add(buildPaymentDetailTable(payment, bold, regular));

            if (payment.notes() != null && !payment.notes().isEmpty()) {
                document.add(buildSectionTitle("OBSERVACIONES", bold));
                document.add(new Paragraph(payment.notes()).setFont(regular).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY));
            }

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating payment PDF", e);
        }
        return baos.toByteArray();
    }

    private Table buildHeader(PaymentReportData payment, PdfFont bold, PdfFont regular) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        header.setMarginBottom(20);

        Cell logoCell = new Cell()
                .add(new Paragraph("CASHLY").setFont(bold).setFontSize(26).setMarginBottom(0))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(logoCell);

        String paymentDate = payment.createdAt().format(DATE_FORMATTER);
        Cell infoCell = new Cell()
                .add(new Paragraph("Pago N: " + payment.paymentNumber()).setFont(bold).setFontSize(11).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(2))
                .add(new Paragraph("Fecha: " + paymentDate).setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.RIGHT).setFontColor(ColorConstants.DARK_GRAY))
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

    private Table buildPaymentDetailTable(PaymentReportData payment, PdfFont bold, PdfFont regular) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        table.setMarginBottom(8);

        table.addCell(buildDetailCell("Estado: ", payment.status() != null ? payment.status() : "-", bold, regular));
        table.addCell(buildDetailCell("Monto: ", "$ " + formatDecimal(payment.amount()), bold, regular));
        
        String reference = payment.referenceNumber() != null ? payment.referenceNumber() : "-";
        table.addCell(buildDetailCell("Referencia: ", reference, bold, regular));

        String status = payment.status() != null ? payment.status() : "-";
        table.addCell(buildDetailCell("Estado: ", status, bold, regular));

        return table;
    }

    private Cell buildDetailCell(String label, String value, PdfFont bold, PdfFont regular) {
        Paragraph p = new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label).setFont(bold).setFontSize(10))
                .add(new com.itextpdf.layout.element.Text(value).setFont(regular).setFontSize(10));

        return new Cell()
                .add(p)
                .setBackgroundColor(LIGHT_GRAY_ROW)
                .setPadding(8)
                .setBorder(new SolidBorder(BORDER_COLOR, 1));
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
