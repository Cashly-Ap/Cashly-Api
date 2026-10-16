package com.cashly.cashly_api.receivables.reporting.application.generate_collections_report_pdf.services;

import com.cashly.cashly_api.receivables.reporting.application.ports.out.CollectionsReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateCollectionsReportPdfUseCaseImpl Unit Tests")
class GenerateCollectionsReportPdfUseCaseImplTest {

    @Mock
    private CollectionsReportPdfPort pdfPort;

    @InjectMocks
    private GenerateCollectionsReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate collections PDF with customer and aging filter")
    void should_generate_collections_pdf() {
        UUID customerId = UUID.randomUUID();
        LocalDate agingFrom = LocalDate.now().minusMonths(3);
        byte[] expectedPdf = "Collections PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(customerId, agingFrom);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }

    @Test
    @DisplayName("should generate collections PDF without filters")
    void should_generate_collections_pdf_without_filters() {
        byte[] expectedPdf = "All collections PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(null, null);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }
}
