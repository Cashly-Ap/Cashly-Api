package com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.web;

import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportRow;
import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportFilter;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.use_case.GeneratePurchasePdfUseCase;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.use_case.GeneratePurchasesReportPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchasesReportController Unit Tests")
class PurchasesReportControllerTest {

    @Mock
    private GeneratePurchasePdfUseCase generatePurchasePdfUseCase;

    @Mock
    private GeneratePurchasesReportPdfUseCase generatePurchasesReportPdfUseCase;



    @InjectMocks
    private PurchasesReportController controller;

    @Test
    @DisplayName("should return purchase PDF as attachment")
    void should_get_purchase_pdf() {
        UUID purchaseId = UUID.randomUUID();
        byte[] pdfContent = "Purchase PDF".getBytes();

        when(generatePurchasePdfUseCase.execute(purchaseId)).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPurchasePdf(purchaseId);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("compra-" + purchaseId + ".pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generatePurchasePdfUseCase).execute(purchaseId);
    }

    @Test
    @DisplayName("should return purchases report PDF as attachment")
    void should_get_purchases_report_pdf() {
        UUID supplierId = UUID.randomUUID();
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] pdfContent = "Purchases report PDF".getBytes();
        List<PurchaseReportRow> purchases = List.of();

        var filter = new PurchaseReportFilter(dateFrom, dateTo, supplierId, "PENDING", productId, categoryId);
        when(generatePurchasesReportPdfUseCase.execute(filter, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPurchasesReportPdf(
                dateFrom, dateTo, supplierId, "PENDING", productId, categoryId, "SIMPLE");

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("reporte-compras-"));
        assertArrayEquals(pdfContent, result.getBody());

        verify(generatePurchasesReportPdfUseCase).execute(filter, "SIMPLE");
    }

    @Test
    @DisplayName("should return purchases report PDF without optional params")
    void should_get_purchases_report_pdf_without_optional_params() {
        byte[] pdfContent = "PDF no filters".getBytes();
        List<PurchaseReportRow> purchases = List.of();

        var filter = new PurchaseReportFilter(null, null, null, null, null, null);
        when(generatePurchasesReportPdfUseCase.execute(filter, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPurchasesReportPdf(
                null, null, null, null, null, null, "SIMPLE");

        assertArrayEquals(pdfContent, result.getBody());
        verify(generatePurchasesReportPdfUseCase).execute(filter, "SIMPLE");
    }
}
