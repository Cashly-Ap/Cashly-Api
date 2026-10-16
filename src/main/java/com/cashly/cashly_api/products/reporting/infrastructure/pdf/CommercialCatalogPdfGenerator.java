package com.cashly.cashly_api.products.reporting.infrastructure.pdf;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.cashly.cashly_api.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.products.reporting.application.ports.out.MediaGateway;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommercialCatalogPdfGenerator implements CommercialCatalogPdfPort {

    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb CATEGORY_BG = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb PLACEHOLDER_BG = new DeviceRgb(230, 230, 230);
    private static final DeviceRgb PRICE_COLOR = new DeviceRgb(30, 30, 30);
    private static final float IMAGE_SIZE = 120f;
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    private final MediaGateway media;

    public CommercialCatalogPdfGenerator(MediaGateway media) { this.media = media; }

    @Override
    public byte[] generatePdf(List<CommercialCatalogResponse> items) {
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

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            Paragraph title = new Paragraph("Cat\u00e1logo Comercial")
                    .setFont(bold)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(2);

            Paragraph subtitle = new Paragraph("Generado el " + fecha)
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);

            document.add(title);
            document.add(subtitle);

            Map<String, List<CommercialCatalogResponse>> grouped = items.stream()
                    .collect(Collectors.groupingBy(
                            CommercialCatalogResponse::categoryName,
                            Collectors.toList()));

            for (Map.Entry<String, List<CommercialCatalogResponse>> entry : grouped.entrySet()) {
                document.add(createCategoryHeader(entry.getKey(), bold));

                for (CommercialCatalogResponse item : entry.getValue()) {
                    document.add(createProductCard(item, bold, regular, media));
                }
            }

            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error generating commercial catalog PDF", e);
        }
        return baos.toByteArray();
    }

    private Paragraph createCategoryHeader(String categoryName, PdfFont bold) {
        return new Paragraph(categoryName)
                .setFont(bold)
                .setFontSize(14)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setBackgroundColor(CATEGORY_BG)
                .setPaddingLeft(8)
                .setPaddingTop(6)
                .setPaddingBottom(6)
                .setMarginTop(12)
                .setMarginBottom(8)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Table createProductCard(CommercialCatalogResponse item, PdfFont bold, PdfFont regular,
                                    MediaGateway fileStorageService) throws IOException {
        float[] columnWidths = {IMAGE_SIZE + 20, 1f};
        Table card = new Table(UnitValue.createPointArray(columnWidths))
                .useAllAvailableWidth()
                .setMarginBottom(8)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(10);

        Cell imageCell = createImageCell(item, regular, fileStorageService);
        Cell infoCell = createInfoCell(item, bold, regular);

        card.addCell(imageCell);
        card.addCell(infoCell);

        return card;
    }

    private Cell createImageCell(CommercialCatalogResponse item, PdfFont regular,
                                  MediaGateway fileStorageService) {
        Cell cell = new Cell()
                .setWidth(IMAGE_SIZE + 20)
                .setHeight(IMAGE_SIZE + 20)
                .setPadding(5)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        try {
            byte[] imageBytes = null;
            if (item.photo() != null && !item.photo().isBlank()) {
                try {
                    imageBytes = fileStorageService.load(item.photo());
                } catch (Exception e) {
                    imageBytes = null;
                }
            }

            if (imageBytes != null && imageBytes.length > 0) {
                ImageData imageData = ImageDataFactory.create(imageBytes);
                Image img = new Image(imageData);
                img.setAutoScale(true);
                img.setMaxWidth(IMAGE_SIZE);
                img.setMaxHeight(IMAGE_SIZE);
                cell.add(img);
            } else {
                Table placeholder = new Table(1)
                        .setWidth(IMAGE_SIZE)
                        .setHeight(IMAGE_SIZE);
                Cell placeholderCell = new Cell()
                        .setBackgroundColor(PLACEHOLDER_BG)
                        .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .add(new Paragraph("Sin imagen")
                                .setFont(regular)
                                .setFontSize(9)
                                .setFontColor(ColorConstants.GRAY));
                placeholder.addCell(placeholderCell);
                cell.add(placeholder);
            }
        } catch (Exception e) {
            Paragraph errorText = new Paragraph("Sin imagen")
                    .setFont(regular)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY);
            cell.add(errorText);
        }

        return cell;
    }

    private Cell createInfoCell(CommercialCatalogResponse item, PdfFont bold, PdfFont regular) {
        Cell cell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(10)
                .setPaddingTop(5)
                .setVerticalAlignment(VerticalAlignment.TOP);

        Paragraph name = new Paragraph(item.productName())
                .setFont(bold)
                .setFontSize(13)
                .setFontColor(ColorConstants.BLACK)
                .setMarginBottom(2);

        Paragraph sku = new Paragraph("SKU: " + item.sku())
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(6);

        cell.add(name);
        cell.add(sku);

        if (item.description() != null && !item.description().isBlank()) {
            Paragraph desc = new Paragraph(item.description())
                    .setFont(regular)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(8);
            cell.add(desc);
        }

        if (item.price() != null) {
            String formattedPrice = "$ " + PRICE_FORMAT.format(item.price());
            Paragraph priceLabel = new Paragraph(formattedPrice)
                    .setFont(bold)
                    .setFontSize(14)
                    .setFontColor(PRICE_COLOR)
                    .setMarginBottom(1);

            String priceTypeLabel = item.priceTypeName() != null
                    ? "Precio " + item.priceTypeName()
                    : "Precio";

            Paragraph priceType = new Paragraph(priceTypeLabel)
                    .setFont(regular)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY);

            cell.add(priceLabel);
            cell.add(priceType);
        }

        return cell;
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
                        new Paragraph("P\u00e1gina " + pageNumber + " de " + totalPages)
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
