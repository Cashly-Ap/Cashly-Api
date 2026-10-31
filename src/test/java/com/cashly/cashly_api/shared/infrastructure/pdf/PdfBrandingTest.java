package com.cashly.cashly_api.shared.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.assertThat;

class PdfBrandingTest {
    @Test
    void extremeRatiosAndMaximumDimensionsFitWithoutCroppingArtwork()
            throws Exception {
        for (int[] size : new int[][]{{4000, 20}, {20, 4000}, {4096, 4096},
                {100, 1000}}) {
            var source = new BufferedImage(size[0], size[1],
                    BufferedImage.TYPE_INT_RGB);
            var image = image(new PdfBranding(() -> png(source)));
            assertThat(image.getImageWidth()).isEqualTo(size[0]);
            assertThat(image.getImageHeight()).isEqualTo(size[1]);
            assertThat(image.getImageScaledHeight()).isLessThanOrEqualTo(65);
            assertThat(image.getImageScaledWidth()).isLessThanOrEqualTo(125);
            assertThat(
                    image.getImageScaledWidth() / image.getImageScaledHeight())
                    .isCloseTo((float) size[0] / size[1],
                            org.assertj.core.data.Offset.offset(0.0001f));
        }
    }
    @Test
    void trimsOnlyTransparentOrOpaqueWhiteOuterMargins() {
        for (int type : new int[]{BufferedImage.TYPE_INT_ARGB,
                BufferedImage.TYPE_INT_RGB}) {
            var source = new BufferedImage(500, 500, type);
            var graphics = source.createGraphics();
            if (type == BufferedImage.TYPE_INT_RGB) {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, 500, 500);
            }
            graphics.setColor(Color.BLACK);
            graphics.fillRect(200, 220, 100, 50);
            graphics.dispose();
            var image = image(new PdfBranding(() -> png(source)));
            assertThat(image.getImageWidth()).isEqualTo(104);
            assertThat(image.getImageHeight()).isEqualTo(54);
        }
    }
    @Test
    void preservesWhiteAndFaintArtworkOnTransparentCanvas() {
        var source = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        var graphics = source.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(200, 200, 40, 40);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(50, 100, 20, 20);
        graphics.setColor(new Color(255, 255, 255, 1));
        graphics.fillRect(400, 400, 20, 20);
        graphics.dispose();
        var image = image(new PdfBranding(() -> png(source)));
        assertThat(image.getImageWidth()).isEqualTo(384);
        assertThat(image.getImageHeight()).isEqualTo(334);
    }
    @Test
    void preservesColoredFullBleedAndBlankImages() {
        for (Color color : new Color[]{Color.BLUE, Color.WHITE}) {
            var source = new BufferedImage(300, 100,
                    BufferedImage.TYPE_INT_RGB);
            var graphics = source.createGraphics();
            graphics.setColor(color);
            graphics.fillRect(0, 0, 300, 100);
            graphics.dispose();
            var image = image(new PdfBranding(() -> png(source)));
            assertThat(image.getImageWidth()).isEqualTo(300);
            assertThat(image.getImageHeight()).isEqualTo(100);
        }
    }
    @Test
    void unconfiguredLogoUsesLiteralTextAndCreatesIndependentLayoutElements()
            throws Exception {
        var branding = new PdfBranding(() -> null);
        var first = branding.logo();
        assertThat(first.getChildren()).hasSize(1);
        assertThat(first.getChildren().getFirst())
                .isInstanceOf(com.itextpdf.layout.element.Paragraph.class);
        assertThat(branding.logo()).isNotSameAs(first);
        var bytes = new ByteArrayOutputStream();
        try (var document = new com.itextpdf.layout.Document(
                new com.itextpdf.kernel.pdf.PdfDocument(
                        new com.itextpdf.kernel.pdf.PdfWriter(bytes)))) {
            document.add(first);
        }
        try (var pdf = new com.itextpdf.kernel.pdf.PdfDocument(
                new com.itextpdf.kernel.pdf.PdfReader(
                        new java.io.ByteArrayInputStream(
                                bytes.toByteArray())))) {
            assertThat(com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
                    .getTextFromPage(pdf.getFirstPage())).isEqualTo("Logotipo");
            assertThat(pdf.getFirstPage().getResources()
                    .getResource(com.itextpdf.kernel.pdf.PdfName.XObject))
                    .isNull();
        }
    }
    private static com.itextpdf.layout.element.Image image(
            PdfBranding branding) {
        return (com.itextpdf.layout.element.Image) branding.logo().getChildren()
                .getFirst();
    }
    private static byte[] png(BufferedImage image) {
        try {
            var bytes = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bytes);
            return bytes.toByteArray();
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
