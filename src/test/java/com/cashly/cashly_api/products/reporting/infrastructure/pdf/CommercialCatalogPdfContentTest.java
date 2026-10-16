package com.cashly.cashly_api.products.reporting.infrastructure.pdf;
import com.cashly.cashly_api.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.products.reporting.application.ports.out.MediaGateway;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CommercialCatalogPdfContentTest {
    private final MediaGateway media = mock(MediaGateway.class);
    private CommercialCatalogResponse item(String photo) {
        return new CommercialCatalogResponse(UUID.randomUUID(), "Product Example", "SKU-123", "Description", photo, UUID.randomUUID(), "Category", new BigDecimal("12.40"), "Retail");
    }
    private String text(byte[] bytes) throws Exception {
        try (var pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            var text = new StringBuilder();
            for (int i = 1; i <= pdf.getNumberOfPages(); i++) text.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)));
            return text.toString();
        }
    }
    @Test void validImagePreservesProductContentAndEmbeddedImage() throws Exception {
        when(media.load("photo.png")).thenReturn(Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jX1sAAAAASUVORK5CYII="));
        var bytes = new CommercialCatalogPdfGenerator(media).generatePdf(List.of(item("photo.png")));
        var content = text(bytes);
        for (var expected : List.of("Product Example", "SKU-123", "Description", "Category", "Retail")) assertTrue(content.contains(expected), expected);
        assertFalse(content.contains("Sin imagen"));
        try (var pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)))) {
            assertNotNull(pdf.getPage(1).getResources().getPdfObject().getAsDictionary(PdfName.XObject));
        }
        verify(media).load("photo.png");
    }
    @Test void missingPhotoPreservesPlaceholderWithoutStorageAccess() throws Exception {
        assertTrue(text(new CommercialCatalogPdfGenerator(media).generatePdf(List.of(item(null)))).contains("Sin imagen"));
        verifyNoInteractions(media);
    }
    @Test void actualIOExceptionPreservesPlaceholderAndProductContent() throws Exception {
        when(media.load("broken.png")).thenAnswer(invocation -> { throw new IOException("Storage unavailable"); });
        var content = text(new CommercialCatalogPdfGenerator(media).generatePdf(List.of(item("broken.png"))));
        assertTrue(content.contains("Sin imagen"));
        assertTrue(content.contains("Product Example"));
        verify(media).load("broken.png");
    }
    @Test void invalidImagePreservesPlaceholder() throws Exception {
        when(media.load("invalid.png")).thenReturn(new byte[]{1, 2, 3});
        assertTrue(text(new CommercialCatalogPdfGenerator(media).generatePdf(List.of(item("invalid.png")))).contains("Sin imagen"));
    }
    @Test void emptyCatalogPreservesTitleWithoutLoadingMedia() throws Exception {
        assertTrue(text(new CommercialCatalogPdfGenerator(media).generatePdf(List.of())).contains("Comercial"));
        verifyNoInteractions(media);
    }
}
