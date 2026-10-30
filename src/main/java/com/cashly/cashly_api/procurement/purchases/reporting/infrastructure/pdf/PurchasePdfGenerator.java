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
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportData;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasePdfPort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PurchasePdfGenerator implements PurchasePdfPort {

    private final PdfBranding branding;

    public PurchasePdfGenerator() { this(new PdfBranding()); }

    public PurchasePdfGenerator(PdfBranding branding) { this.branding = branding; }

    private static final DeviceRgb LIGHT_GRAY_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_GREEN = new DeviceRgb(34, 139, 34);
    private static final DeviceRgb COLOR_RED = new DeviceRgb(220, 20, 60);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(200, 200, 200);


    @Override
    public byte[] generatePdf(PurchaseReportData purchase) {
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

            document.add(buildHeader(purchase, bold, regular));
            document.add(buildSectionTitle(ReportMessages.text("procurement.purchases.reports.supplierDetails"), bold));
            document.add(buildSupplierTable(purchase, bold, regular));
            document.add(buildSectionTitle(ReportMessages.text("procurement.purchases.reports.purchaseDetails"), bold));
            document.add(buildLinesTable(purchase, bold, regular));
            document.add(buildTotalsTable(purchase, bold, regular));

            if (purchase.notes() != null && !purchase.notes().isEmpty()) {
                document.add(buildSectionTitle(ReportMessages.text("procurement.purchases.reports.notes"), bold));
                document.add(new Paragraph(purchase.notes()).setFont(regular).setFontSize(10).setFontColor(ColorConstants.DARK_GRAY));
            }

            if (purchase.status().equals("CANCELLED") && purchase.cancelledReason() != null) {
                document.add(buildSectionTitle(ReportMessages.text("procurement.purchases.reports.cancellationReason"), bold));
                document.add(new Paragraph(purchase.cancelledReason()).setFont(regular).setFontSize(10).setFontColor(COLOR_RED));
            }

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating purchase PDF", e);
        }
        return baos.toByteArray();
    }

    private Table buildHeader(PurchaseReportData purchase, PdfFont bold, PdfFont regular) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        header.setMarginBottom(20);

        Cell logoCell = new Cell()
                .add(branding.logo())
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(logoCell);

        String purchaseDate = purchase.createdAt().atZone(ZoneId.systemDefault()).format(ReportMessages.dateTimeFormatter());
        Cell infoCell = new Cell()
                .add(new Paragraph(ReportMessages.text("procurement.purchases.reports.purchaseNo") + purchase.purchaseNumber()).setFont(bold).setFontSize(11).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(2))
                .add(new Paragraph(ReportMessages.text("procurement.purchases.reports.date") + purchaseDate).setFont(regular).setFontSize(10).setTextAlignment(TextAlignment.RIGHT).setFontColor(ColorConstants.DARK_GRAY))
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

    private Table buildSupplierTable(PurchaseReportData purchase, PdfFont bold, PdfFont regular) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{50f, 50f})).useAllAvailableWidth();
        table.setMarginBottom(8);

        table.addCell(buildSupplierCell(ReportMessages.text("procurement.purchases.reports.supplier"), purchase.supplierName(), bold, regular, true));
        table.addCell(buildSupplierCell(ReportMessages.text("procurement.purchases.reports.taxId"), purchase.supplierRif() != null ? purchase.supplierRif() : "-", bold, regular, false));

        return table;
    }

    private Cell buildSupplierCell(String label, String value, PdfFont bold, PdfFont regular, boolean leftBorder) {
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

    private Table buildLinesTable(PurchaseReportData purchase, PdfFont bold, PdfFont regular) {
        float[] columnWidths = {2f, 4f, 2f, 2f, 2f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
        table.setMarginBottom(8);

        String[] headers = {ReportMessages.text("procurement.purchases.reports.code"), ReportMessages.text("procurement.purchases.reports.description"), ReportMessages.text("procurement.purchases.reports.quantity"), ReportMessages.text("procurement.purchases.reports.unitCost"), ReportMessages.text("procurement.purchases.reports.total")};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(HEADER_TEXT))
                    .setBackgroundColor(HEADER_BG)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(6)
                    .setBorder(Border.NO_BORDER));
        }

        for (int i = 0; i < purchase.lines().size(); i++) {
            var line = purchase.lines().get(i);
            DeviceRgb rowBg = (i % 2 != 0) ? LIGHT_GRAY_ROW : null;

            addTableCell(table, line.productCode() != null ? line.productCode() : "-", regular, rowBg, TextAlignment.CENTER);
            addTableCell(table, line.productName(), regular, rowBg, TextAlignment.LEFT);
            addTableCell(table, formatDecimal(line.quantity()), regular, rowBg, TextAlignment.CENTER);
            addTableCell(table, "$ " + formatDecimal(line.unitCost()), regular, rowBg, TextAlignment.RIGHT);
            addTableCell(table, "$ " + formatDecimal(line.subtotal()), regular, rowBg, TextAlignment.RIGHT);
        }

        return table;
    }

    private Table buildTotalsTable(PurchaseReportData purchase, PdfFont bold, PdfFont regular) {
        float[] widths = {70f, 30f};
        Table table = new Table(UnitValue.createPercentArray(widths));
        table.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
        table.setWidth(UnitValue.createPercentValue(45));
        table.setMarginTop(8);
        table.setBorder(new SolidBorder(BORDER_COLOR, 1));

        addTotalsRow(table, ReportMessages.text("procurement.purchases.reports.subtotal"), "$ " + formatDecimal(purchase.subtotal()), bold, regular, null);

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
        return String.format(ReportMessages.locale(), "%.2f", value);
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
                        new Paragraph(ReportMessages.text("procurement.purchases.reports.page") + pageNumber + ReportMessages.text("procurement.purchases.reports.of") + totalPages)
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
