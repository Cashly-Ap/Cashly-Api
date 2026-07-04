package com.parsun.parsun_api.reports.inventory.application.usecases;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.parsun.parsun_api.reports.inventory.application.port.CommercialCatalogRepositoryPort;
import com.parsun.parsun_api.reports.inventory.application.query.CommercialCatalogQuery;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
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

    @Mock
    private FileStorageService fileStorageService;

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
        when(pdfPort.generatePdf(items, fileStorageService)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repositoryPort).findCatalog(query);
        verify(pdfPort).generatePdf(items, fileStorageService);
    }

    @Test
    @DisplayName("should generate commercial catalog PDF with empty result")
    void should_generate_commercial_catalog_pdf_with_empty_result() {
        CommercialCatalogQuery query = new CommercialCatalogQuery(null, null, null, null);
        List<CommercialCatalogResponse> items = List.of();
        byte[] expectedPdf = "Empty PDF".getBytes();

        when(repositoryPort.findCatalog(query)).thenReturn(items);
        when(pdfPort.generatePdf(items, fileStorageService)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(query);

        assertArrayEquals(expectedPdf, result);
        verify(repositoryPort).findCatalog(query);
        verify(pdfPort).generatePdf(items, fileStorageService);
    }
}
