package com.cashly.cashly_api.purchases.reporting.application.generate_purchases_report_pdf;

import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportRow;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchasesReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratePurchasesReportPdfUseCaseImpl Unit Tests")
class GeneratePurchasesReportPdfUseCaseImplTest {

    @Mock
    private PurchasesReportPdfPort purchasesReportPdfPort;

    @Mock private PurchaseReportQueryGateway query;
    @InjectMocks
    private GeneratePurchasesReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate purchases report PDF with mode")
    void should_generate_purchases_report_pdf() {
        List<PurchaseReportRow> purchases = List.of(mock(PurchaseReportRow.class));
        var filter = new PurchaseReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(purchases);
        byte[] expectedPdf = "Purchases report PDF".getBytes();

        when(purchasesReportPdfPort.generatePdf(purchases, "SIMPLE")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, "SIMPLE");

        assertArrayEquals(expectedPdf, result);
        verify(purchasesReportPdfPort).generatePdf(purchases, "SIMPLE");
    }

    @Test
    @DisplayName("should generate purchases report PDF with null mode defaulting to simple")
    void should_generate_purchases_report_pdf_with_null_mode() {
        List<PurchaseReportRow> purchases = List.of();
        var filter = new PurchaseReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(purchases);
        byte[] expectedPdf = "Default PDF".getBytes();

        when(purchasesReportPdfPort.generatePdf(purchases, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, null);

        assertArrayEquals(expectedPdf, result);
        verify(purchasesReportPdfPort).generatePdf(purchases, "simple");
    }
}
