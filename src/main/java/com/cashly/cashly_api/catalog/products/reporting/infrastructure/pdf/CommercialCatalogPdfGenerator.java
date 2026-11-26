package com.cashly.cashly_api.catalog.products.reporting.infrastructure.pdf;

import com.cashly.cashly_api.catalog.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.catalog.products.reporting.application.ports.out.MediaGateway;
import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.io.font.constants.StandardFonts;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommercialCatalogPdfGenerator implements CommercialCatalogPdfPort {

    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(210, 210,
            210);
    private static final DeviceRgb CATEGORY_BG = new DeviceRgb(240, 240, 240);
    private static final DeviceRgb PLACEHOLDER_BG = new DeviceRgb(230, 230,
            230);
    private static final DeviceRgb PRICE_COLOR = new DeviceRgb(30, 30, 30);
    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat(
            "#,##0.00");

    private static final float MARGIN = 40f;
    private static final float CONTENT_START_GAP = 120f;
    private static final float CONTENT_TOP_GAP = 10f;
    private static final float IMAGE_SIZE = 120f;
    private static final float CARD_HEIGHT = 140f;
    private static final float CARD_GAP = 8f;
    private static final float CATEGORY_HEIGHT = 24f;
    private static final float CATEGORY_GAP = 8f;
    private static final float BOTTOM_LIMIT = 40f;

    private final MediaGateway media;
    private final PdfBranding branding;

    public CommercialCatalogPdfGenerator(MediaGateway media) {
        this(media, new PdfBranding());
    }

    public CommercialCatalogPdfGenerator(MediaGateway media,
            PdfBranding branding) {
        this.media = media;
        this.branding = branding;
    }

    @Override
    public byte[] generatePdf(List<CommercialCatalogResponse> items) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            PageSize size = PageSize.A4;
            pdf.setDefaultPageSize(size);
            PdfFont bold = PdfFontFactory.createFont(
                    StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            float contentWidth = size.getWidth() - 2 * MARGIN;

            Cursor cursor = new Cursor(pdf, size);
            drawLogo(cursor.canvas, size);
            drawText(cursor.canvas, ReportMessages.text(
                    "catalog.products.reports.commercialCatalog"), bold, 20f,
                    ColorConstants.BLACK, MARGIN,
                    size.getHeight() - MARGIN - 70f, contentWidth);
            drawText(cursor.canvas, ReportMessages.text(
                    "catalog.products.reports.generatedOn")
                    + LocalDate.now().format(ReportMessages.dateFormatter()),
                    regular, 10f, ColorConstants.DARK_GRAY, MARGIN,
                    size.getHeight() - MARGIN - 88f, contentWidth);
            cursor.y = size.getHeight() - MARGIN - CONTENT_START_GAP;

            Map<String, List<CommercialCatalogResponse>> grouped = new LinkedHashMap<>();
            for (CommercialCatalogResponse item : items) {
                grouped.computeIfAbsent(
                        item.categoryName() != null ? item.categoryName() : "-",
                        key -> new ArrayList<>()).add(item);
            }

            for (Map.Entry<String, List<CommercialCatalogResponse>> entry : grouped
                    .entrySet()) {
                cursor.ensure(CATEGORY_HEIGHT + CATEGORY_GAP);
                drawCategoryHeader(cursor, entry.getKey(), bold, contentWidth);
                for (CommercialCatalogResponse item : entry.getValue()) {
                    cursor.ensure(CARD_HEIGHT + CARD_GAP);
                    drawCard(cursor, item, bold, regular, contentWidth);
                }
            }

            drawFooters(pdf, size, regular);
            pdf.close();
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Error generating commercial catalog PDF", exception);
        }
        return output.toByteArray();
    }

    private void drawLogo(PdfCanvas canvas, PageSize size) {
        byte[] bytes = branding.logoBytes();
        if (bytes == null) {
            return;
        }
        ImageData image = ImageDataFactory.create(bytes);
        float height = 45f;
        float width = Math.min(125f,
                image.getWidth() * height / Math.max(1f, image.getHeight()));
        canvas.addImageFittedIntoRectangle(image,
                new Rectangle(MARGIN, size.getHeight() - MARGIN - height, width,
                        height),
                false);
    }

    private void drawCategoryHeader(Cursor cursor, String name, PdfFont bold,
            float contentWidth) {
        cursor.canvas.setFillColor(CATEGORY_BG);
        cursor.canvas.rectangle(MARGIN, cursor.y - CATEGORY_HEIGHT, contentWidth,
                CATEGORY_HEIGHT).fill();
        drawText(cursor.canvas, name, bold, 13f, ColorConstants.DARK_GRAY,
                MARGIN + 8f, cursor.y - 16f, contentWidth - 16f);
        cursor.y -= CATEGORY_HEIGHT + CATEGORY_GAP;
    }

    private void drawCard(Cursor cursor, CommercialCatalogResponse item,
            PdfFont bold, PdfFont regular, float contentWidth) {
        float top = cursor.y;
        float bottom = top - CARD_HEIGHT;
        cursor.canvas.setStrokeColor(BORDER_COLOR).setLineWidth(0.5f)
                .rectangle(MARGIN, bottom, contentWidth, CARD_HEIGHT).stroke();

        float imageX = MARGIN + 10f;
        float imageY = bottom + 10f;
        ImageData image = loadImage(item.photo());
        if (image != null) {
            cursor.canvas.addImageFittedIntoRectangle(image,
                    new Rectangle(imageX, imageY, IMAGE_SIZE, IMAGE_SIZE),
                    false);
        } else {
            drawPlaceholder(cursor.canvas, imageX, imageY, regular);
        }

        float infoX = imageX + IMAGE_SIZE + 12f;
        float infoWidth = MARGIN + contentWidth - 10f - infoX;
        drawText(cursor.canvas, item.productName(), bold, 13f,
                ColorConstants.BLACK, infoX, top - 26f, infoWidth);
        drawText(cursor.canvas, ReportMessages.text(
                "catalog.products.reports.sku") + item.sku(), regular, 9f,
                ColorConstants.DARK_GRAY, infoX, top - 44f, infoWidth);

        if (item.description() != null && !item.description().isBlank()) {
            drawWrapped(cursor.canvas, item.description(), regular, 10f,
                    ColorConstants.DARK_GRAY, infoX, top - 62f, infoWidth, 2);
        }

        if (item.price() != null) {
            drawText(cursor.canvas,
                    "$ " + PRICE_FORMAT.format(item.price()), bold, 14f,
                    PRICE_COLOR, infoX, top - 104f, infoWidth);
            String priceTypeLabel = item.priceTypeName() != null
                    ? ReportMessages.text("catalog.products.reports.price")
                            + item.priceTypeName()
                    : ReportMessages
                            .text("catalog.products.reports.priceLabel");
            drawText(cursor.canvas, priceTypeLabel, regular, 8f,
                    ColorConstants.GRAY, infoX, top - 120f, infoWidth);
        }
        cursor.y -= CARD_HEIGHT + CARD_GAP;
    }

    private ImageData loadImage(String photo) {
        if (photo == null || photo.isBlank()) {
            return null;
        }
        try {
            byte[] bytes = media.load(photo);
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return ImageDataFactory.create(bytes);
        } catch (Exception exception) {
            return null;
        }
    }

    private void drawPlaceholder(PdfCanvas canvas, float x, float y,
            PdfFont regular) {
        canvas.setFillColor(PLACEHOLDER_BG);
        canvas.rectangle(x, y, IMAGE_SIZE, IMAGE_SIZE).fill();
        drawCentered(canvas, ReportMessages.text(
                "catalog.products.reports.noImage"), regular, 9f,
                ColorConstants.GRAY, x + IMAGE_SIZE / 2f, y + IMAGE_SIZE / 2f,
                IMAGE_SIZE - 10f);
    }

    private void drawFooters(PdfDocument pdf, PageSize size, PdfFont regular) {
        int total = pdf.getNumberOfPages();
        for (int index = 1; index <= total; index++) {
            PdfPage page = pdf.getPage(index);
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(),
                    page.getResources(), pdf);
            drawCentered(canvas, "P\u00e1gina " + index
                    + ReportMessages.text("catalog.products.reports.of") + total,
                    regular, 8f, ColorConstants.GRAY, size.getWidth() / 2f,
                    24f, size.getWidth() - 2 * MARGIN);
        }
    }

    private static void drawText(PdfCanvas canvas, String text, PdfFont font,
            float size, Color color, float x, float y, float maxWidth) {
        String value = truncate(text == null ? "" : text, font, size, maxWidth);
        canvas.setFillColor(color == null ? ColorConstants.BLACK : color);
        canvas.beginText().setFontAndSize(font, size).moveText(x, y)
                .showText(value).endText();
    }

    private static void drawCentered(PdfCanvas canvas, String text, PdfFont font,
            float size, Color color, float centerX, float y, float maxWidth) {
        String value = truncate(text == null ? "" : text, font, size, maxWidth);
        float x = centerX - font.getWidth(value, size) / 2f;
        canvas.setFillColor(color == null ? ColorConstants.BLACK : color);
        canvas.beginText().setFontAndSize(font, size).moveText(x, y)
                .showText(value).endText();
    }

    private static void drawWrapped(PdfCanvas canvas, String text, PdfFont font,
            float size, Color color, float x, float y, float maxWidth,
            int maxLines) {
        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();
        int line = 0;
        float cursorY = y;
        for (int index = 0; index < words.length && line < maxLines; index++) {
            String candidate = current.isEmpty()
                    ? words[index]
                    : current + " " + words[index];
            if (font.getWidth(candidate, size) > maxWidth && !current.isEmpty()) {
                boolean last = line == maxLines - 1 || index == words.length - 1;
                drawText(canvas, last ? current + "\u2026" : current.toString(),
                        font, size, color, x, cursorY, maxWidth);
                if (last) {
                    return;
                }
                current = new StringBuilder(words[index]);
                cursorY -= size + 3f;
                line++;
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (line < maxLines && !current.isEmpty()) {
            drawText(canvas, current.toString(), font, size, color, x, cursorY,
                    maxWidth);
        }
    }

    private static String truncate(String value, PdfFont font, float size,
            float maxWidth) {
        if (font.getWidth(value, size) <= maxWidth) {
            return value;
        }
        String ellipsis = "\u2026";
        int length = value.length();
        while (length > 0 && font.getWidth(
                value.substring(0, length) + ellipsis, size) > maxWidth) {
            length--;
        }
        return value.substring(0, length) + ellipsis;
    }

    private static final class Cursor {
        private final PdfDocument pdf;
        private final PageSize size;
        private PdfCanvas canvas;
        private float y;

        Cursor(PdfDocument pdf, PageSize size) {
            this.pdf = pdf;
            this.size = size;
            this.canvas = new PdfCanvas(pdf.addNewPage(size));
            this.y = size.getHeight() - MARGIN;
        }

        void ensure(float needed) {
            if (y - needed < BOTTOM_LIMIT) {
                canvas = new PdfCanvas(pdf.addNewPage(size));
                y = size.getHeight() - MARGIN - CONTENT_TOP_GAP;
            }
        }
    }
}
