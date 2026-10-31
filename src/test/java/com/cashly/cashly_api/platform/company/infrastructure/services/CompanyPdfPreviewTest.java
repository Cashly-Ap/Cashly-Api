package com.cashly.cashly_api.platform.company.infrastructure.services;

import com.cashly.cashly_api.platform.company.application.services.CompanyService;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyPdfPreviewTest {
    @Test
    void previewUsesRealInvoiceAndSharedTrimWithoutSavingAnything()
            throws Exception {
        var image = new BufferedImage(1000, 400, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(200, 150, 600, 100);
        graphics.dispose();
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        var company = mock(CompanyService.class);
        when(company.previewDocumentLogo(any(), eq(false), isNull(), eq(false)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var bytes = new CompanyPdfPreview(company)
                .generate(output.toByteArray(), false, null, false);
        java.nio.file.Files.write(
                java.nio.file.Path.of("target/company-logo-preview.pdf"),
                bytes);
        try (var pdf = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(bytes)))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
            var text = PdfTextExtractor.getTextFromPage(pdf.getFirstPage());
            assertThat(text).contains("VISTA PREVIA", "Cliente de ejemplo",
                    "150,00");
            var objects = pdf.getFirstPage().getResources()
                    .getResource(PdfName.XObject);
            assertThat(objects).isNotNull();
            boolean trimmed = false;
            for (var name : objects.keySet()) {
                var stream = objects.getAsStream(name);
                if (PdfName.Image.equals(stream.getAsName(PdfName.Subtype))) {
                    trimmed |= stream.getAsNumber(PdfName.Width)
                            .intValue() == 624
                            && stream.getAsNumber(PdfName.Height)
                                    .intValue() == 124;
                }
            }
            assertThat(trimmed).isTrue();
        }
        verify(company).previewDocumentLogo(any(), eq(false), isNull(),
                eq(false));
        verifyNoMoreInteractions(company);
    }
    @Test
    void previewWithoutConfiguredLogoDisplaysLiteralLogotipo()
            throws Exception {
        var company = mock(CompanyService.class);
        var bytes = new CompanyPdfPreview(company).generate(null, true, null,
                true);
        try (var pdf = new PdfDocument(
                new PdfReader(new ByteArrayInputStream(bytes)))) {
            assertThat(PdfTextExtractor.getTextFromPage(pdf.getFirstPage()))
                    .contains("Logotipo");
            assertThat(pdf.getFirstPage().getResources()
                    .getResource(PdfName.XObject)).isNull();
        }
    }
    @Test
    void largePreviewUsesTheSameReducedBytesAsSaveNormalization()
            throws Exception {
        var image = new BufferedImage(6000, 1000, BufferedImage.TYPE_INT_RGB);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        var original = output.toByteArray();
        var company = mock(CompanyService.class);
        when(company.previewDocumentLogo(any(), eq(false), isNull(), eq(false)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        new CompanyPdfPreview(company).generate(original, false, null, false);
        var captured = org.mockito.ArgumentCaptor.forClass(byte[].class);
        verify(company).previewDocumentLogo(captured.capture(), eq(false),
                isNull(), eq(false));
        assertThat(captured.getValue()).containsExactly(
                com.cashly.cashly_api.shared.storage.infrastructure.images.SafeImageStorage
                        .normalize(original));
        var decoded = ImageIO
                .read(new ByteArrayInputStream(captured.getValue()));
        assertThat(decoded.getWidth()).isEqualTo(2048);
        assertThat(decoded.getHeight()).isEqualTo(341);
    }
    @Test
    void rejectsMalformedPreviewFilesBeforeSelectingStoredLogo() {
        var company = mock(CompanyService.class);
        var preview = new CompanyPdfPreview(company);
        assertThatIllegalArgumentException().isThrownBy(
                () -> preview.generate(new byte[]{1, 2}, false, null, false));
        assertThatIllegalArgumentException().isThrownBy(() -> preview
                .generate(null, false, new byte[5 * 1024 * 1024 + 1], false));
        verifyNoInteractions(company);
    }
}
