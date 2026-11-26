package com.cashly.cashly_api.shared.infrastructure.pdf;

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
import java.util.List;

public final class FastTablePdfRenderer {

    private static final DeviceRgb ZEBRA_ROW = new DeviceRgb(245, 245, 245);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb GRID_LINE = new DeviceRgb(220, 220, 220);
    private static final float MARGIN = 40f;
    private static final float ROW_HEIGHT = 15f;
    private static final float HEADER_HEIGHT = 18f;
    private static final float FONT_SIZE = 8f;

    private FastTablePdfRenderer() {
    }

    public record Column(String header, float weight, TextAlignment alignment) {
    }

    public static byte[] render(PdfBranding branding, PageSize size,
            String title, String subtitle, List<Column> columns,
            List<String[]> rows, List<String> trailingLines, String pageLabel,
            String ofLabel) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(output));
            pdf.setDefaultPageSize(size);
            PdfFont bold = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

            float contentWidth = size.getWidth() - 2 * MARGIN;
            float[] widths = widths(columns, contentWidth);
            TextAlignment[] alignments = columns.stream()
                    .map(Column::alignment)
                    .toArray(TextAlignment[]::new);
            String[] headers = columns.stream()
                    .map(Column::header)
                    .toArray(String[]::new);

            float firstTableTop = size.getHeight() - MARGIN - 120f;
            float nextTableTop = size.getHeight() - MARGIN - 10f;
            int firstRows = rowsThatFit(firstTableTop, size.getHeight());
            int nextRows = rowsThatFit(nextTableTop, size.getHeight());
            int pages = pages(rows.size(), firstRows, nextRows);

            int index = 0;
            for (int pageNumber = 1; pageNumber <= pages; pageNumber++) {
                PdfPage page = pdf.addNewPage(size);
                PdfCanvas canvas = new PdfCanvas(page);
                boolean first = pageNumber == 1;
                float tableTop = first ? firstTableTop : nextTableTop;
                if (first) {
                    drawLogo(canvas, branding, size);
                    drawText(canvas, title, bold, 18f,
                            ColorConstants.BLACK, MARGIN,
                            size.getHeight() - MARGIN - 70f, contentWidth,
                            TextAlignment.LEFT);
                    drawText(canvas, subtitle, regular, 9f,
                            ColorConstants.DARK_GRAY, MARGIN,
                            size.getHeight() - MARGIN - 88f, contentWidth,
                            TextAlignment.LEFT);
                }
                drawHeader(canvas, headers, widths, alignments, bold, tableTop);
                float y = tableTop - HEADER_HEIGHT - ROW_HEIGHT + 4f;
                int capacity = first ? firstRows : nextRows;
                for (int row = 0; row < capacity && index < rows.size();
                        row++, index++) {
                    if (row % 2 != 0) {
                        fillRow(canvas, y, contentWidth);
                    }
                    drawRow(canvas, rows.get(index), widths, alignments,
                            regular, y);
                    drawGrid(canvas, y - 4f, widths, contentWidth);
                    y -= ROW_HEIGHT;
                }
                if (index >= rows.size() && trailingLines != null
                        && !trailingLines.isEmpty()) {
                    float lineY = y - 6f;
                    for (String line : trailingLines) {
                        if (line == null || line.isBlank()) {
                            lineY -= ROW_HEIGHT;
                            continue;
                        }
                        drawText(canvas, line, bold, 10f,
                                ColorConstants.BLACK, MARGIN, lineY,
                                contentWidth, TextAlignment.RIGHT);
                        lineY -= ROW_HEIGHT;
                    }
                }
                drawFooter(canvas, size, pageNumber, pages, regular, pageLabel,
                        ofLabel);
            }
            pdf.close();
        } catch (IOException exception) {
            throw new RuntimeException("Error generating PDF report",
                    exception);
        }
        return output.toByteArray();
    }

    private static int rowsThatFit(float tableTop, float pageHeight) {
        float available = tableTop - HEADER_HEIGHT - (MARGIN + 40f);
        return Math.max(1, (int) (available / ROW_HEIGHT));
    }

    private static int pages(int rows, int firstRows, int nextRows) {
        if (rows <= firstRows) {
            return 1;
        }
        return 1 + (rows - firstRows + nextRows - 1) / nextRows;
    }

    private static void drawLogo(PdfCanvas canvas, PdfBranding branding,
            PageSize size) {
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

    private static float[] widths(List<Column> columns, float total) {
        float sum = 0f;
        for (Column column : columns) {
            sum += column.weight();
        }
        float[] widths = new float[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = columns.get(i).weight() / sum * total;
        }
        return widths;
    }

    private static void drawHeader(PdfCanvas canvas, String[] headers,
            float[] widths, TextAlignment[] alignments, PdfFont bold,
            float top) {
        float total = 0f;
        for (float width : widths) {
            total += width;
        }
        canvas.setFillColor(HEADER_BG);
        canvas.rectangle(MARGIN, top - HEADER_HEIGHT, total, HEADER_HEIGHT)
                .fill();
        float x = MARGIN + 4f;
        for (int i = 0; i < headers.length; i++) {
            drawText(canvas, headers[i], bold, 9f, HEADER_TEXT, x,
                    top - HEADER_HEIGHT + 5f, widths[i] - 8f,
                    TextAlignment.CENTER);
            x += widths[i];
        }
    }

    private static void drawRow(PdfCanvas canvas, String[] values,
            float[] widths, TextAlignment[] alignments, PdfFont regular,
            float y) {
        float x = MARGIN + 4f;
        for (int i = 0; i < values.length && i < widths.length; i++) {
            drawText(canvas, values[i], regular, FONT_SIZE,
                    ColorConstants.BLACK, x, y, widths[i] - 8f,
                    alignments[i]);
            x += widths[i];
        }
    }

    private static void fillRow(PdfCanvas canvas, float y, float width) {
        canvas.setFillColor(ZEBRA_ROW);
        canvas.rectangle(MARGIN, y - 4f, width, ROW_HEIGHT).fill();
    }

    private static void drawGrid(PdfCanvas canvas, float y, float[] widths,
            float total) {
        canvas.setStrokeColor(GRID_LINE).setLineWidth(0.3f);
        canvas.moveTo(MARGIN, y).lineTo(MARGIN + total, y).stroke();
        float x = MARGIN;
        for (float width : widths) {
            x += width;
            canvas.moveTo(x, y).lineTo(x, y + ROW_HEIGHT).stroke();
        }
        canvas.moveTo(MARGIN, y).lineTo(MARGIN, y + ROW_HEIGHT).stroke();
    }

    private static void drawFooter(PdfCanvas canvas, PageSize size,
            int pageNumber, int totalPages, PdfFont regular, String pageLabel,
            String ofLabel) {
        drawText(canvas, pageLabel + pageNumber + ofLabel + totalPages, regular,
                8f, ColorConstants.GRAY, MARGIN, MARGIN - 20f,
                size.getWidth() - 2 * MARGIN, TextAlignment.CENTER);
    }

    private static void drawText(PdfCanvas canvas, String text, PdfFont font,
            float size, Color color, float x, float y, float maxWidth,
            TextAlignment alignment) {
        String value = text == null ? "" : text;
        value = truncate(value, font, size, Math.max(4f, maxWidth));
        float drawX = x;
        float textWidth = font.getWidth(value, size);
        if (alignment == TextAlignment.RIGHT) {
            drawX = x + maxWidth - textWidth;
        } else if (alignment == TextAlignment.CENTER) {
            drawX = x + (maxWidth - textWidth) / 2f;
        }
        canvas.setFillColor(color == null ? ColorConstants.BLACK : color);
        canvas.beginText().setFontAndSize(font, size).moveText(drawX, y)
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
}
