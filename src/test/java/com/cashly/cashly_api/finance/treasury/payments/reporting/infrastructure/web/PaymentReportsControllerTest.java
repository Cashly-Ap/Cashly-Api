package com.cashly.cashly_api.finance.treasury.payments.reporting.infrastructure.web;

import com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_history_report_pdf.use_case.GeneratePaymentHistoryReportPdfUseCase;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_pdf.use_case.GeneratePaymentPdfUseCase;
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
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReportsControllerTest {

    @Mock
    private GeneratePaymentPdfUseCase generatePaymentPdfUseCase;

    @Mock
    private GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase;

    @InjectMocks
    private PaymentReportsController controller;

    @Test
    @DisplayName("should return payment PDF as attachment")
    void shouldGetPaymentPdf() {
        UUID paymentId = UUID.randomUUID();
        byte[] pdfContent = "PDF content".getBytes();

        when(generatePaymentPdfUseCase.execute(paymentId))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPaymentPdf(paymentId);

        assertEquals(MediaType.APPLICATION_PDF,
                result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)
                .get(0).contains(
                        "attachment; filename=\"pago-" + paymentId + ".pdf\""));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generatePaymentPdfUseCase).execute(paymentId);
    }

    @Test
    @DisplayName("should return payment history PDF as attachment")
    void shouldGetPaymentHistoryPdf() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID customerId = UUID.randomUUID();
        byte[] pdfContent = "History PDF".getBytes();

        when(generatePaymentHistoryReportPdfUseCase.execute(dateFrom, dateTo,
                customerId, "APPLIED", "CASH")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getPaymentHistoryPdf(
                dateFrom, dateTo, customerId, "APPLIED", "CASH");

        assertEquals(MediaType.APPLICATION_PDF,
                result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION)
                .get(0).contains("historial-pagos-"));
        assertArrayEquals(pdfContent, result.getBody());
        verify(generatePaymentHistoryReportPdfUseCase).execute(dateFrom, dateTo,
                customerId, "APPLIED", "CASH");
    }
}
