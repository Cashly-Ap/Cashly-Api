package com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_history_report_pdf.services;

import com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out.PaymentHistoryReportPdfPort;
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
@DisplayName("GeneratePaymentHistoryReportPdfUseCaseImpl Unit Tests")
class GeneratePaymentHistoryReportPdfUseCaseImplTest {

    @Mock
    private PaymentHistoryReportPdfPort pdfPort;

    @InjectMocks
    private GeneratePaymentHistoryReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate payment history PDF with all filters")
    void should_generate_payment_history_pdf() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID customerId = UUID.randomUUID();
        byte[] expectedPdf = "History PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(dateFrom, dateTo, customerId, "APPLIED", "CASH");

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }

    @Test
    @DisplayName("should generate payment history PDF without filters")
    void should_generate_payment_history_pdf_without_filters() {
        byte[] expectedPdf = "All history PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(null, null, null, null, null);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }
}
