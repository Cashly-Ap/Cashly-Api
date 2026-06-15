package com.parsun.parsun_api.reports.finance.application.usecases;

import com.parsun.parsun_api.reports.finance.application.port.AccountsReceivableReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateAccountsReceivableReportPdfUseCaseImpl Unit Tests")
class GenerateAccountsReceivableReportPdfUseCaseImplTest {

    @Mock
    private AccountsReceivableReportPdfPort pdfPort;

    @InjectMocks
    private GenerateAccountsReceivableReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate accounts receivable PDF for customer")
    void should_generate_accounts_receivable_pdf() {
        UUID customerId = UUID.randomUUID();
        byte[] expectedPdf = "AR PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(customerId, false);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }

    @Test
    @DisplayName("should generate accounts receivable PDF with overdue filter")
    void should_generate_overdue_accounts_receivable_pdf() {
        byte[] expectedPdf = "Overdue PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(null, true);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }

    @Test
    @DisplayName("should generate accounts receivable PDF without filters")
    void should_generate_accounts_receivable_pdf_without_filters() {
        byte[] expectedPdf = "All AR PDF".getBytes();

        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(null, false);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }
}
