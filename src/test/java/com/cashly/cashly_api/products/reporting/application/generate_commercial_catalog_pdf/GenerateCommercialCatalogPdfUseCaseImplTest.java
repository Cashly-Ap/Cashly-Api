package com.cashly.cashly_api.products.reporting.application.generate_commercial_catalog_pdf;

import com.cashly.cashly_api.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.products.reporting.application.ports.out.CommercialCatalogRepositoryPort;
import com.cashly.cashly_api.products.reporting.application.dto.query.CommercialCatalogQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateCommercialCatalogPdfUseCaseImpl Unit Tests")
class GenerateCommercialCatalogPdfUseCaseImplTest {

    @Mock
    private CommercialCatalogRepositoryPort repositoryPort;

    @Mock
    private CommercialCatalogPdfPort pdfPort;



    @InjectMocks
    private GenerateCommercialCatalogPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate commercial catalog PDF from query")
    void should_generate_commercial_catalog_pdf() {
        CommercialCatalogQuery query = new CommercialCatalogQuery("test", UUID.randomUUID(), UUID.randomUUID(), true);
        CommercialCatalogResponse item = new CommercialCatalogResponse(
                UUID.randomUUID(), "Product", "SKU", "Desc", "photo.jpg",
                UUID.randomUUID(), "Category", null, null);
        List<CommercialCatalogResponse> items = List.of(item);
        byte[] expectedPdf = "PDF".getBytes();

        when(repositoryPort.findCatalog(query)).thenReturn(items);
        when(pdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repositoryPort).findCatalog(query);
        verify(pdfPort).generatePdf(items);
    }

    @Test
    @DisplayName("should generate commercial catalog PDF with empty result")
    void should_generate_commercial_catalog_pdf_with_empty_result() {
        CommercialCatalogQuery query = new CommercialCatalogQuery(null, null, null, null);
        List<CommercialCatalogResponse> items = List.of();
        byte[] expectedPdf = "Empty PDF".getBytes();

        when(repositoryPort.findCatalog(query)).thenReturn(items);
        when(pdfPort.generatePdf(items)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repositoryPort).findCatalog(query);
        verify(pdfPort).generatePdf(items);
    }
}
