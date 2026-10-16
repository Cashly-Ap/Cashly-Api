package com.cashly.cashly_api.receivables.reporting.application.generate_accounts_receivable_report_pdf.services;

import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableQueryGateway;
import com.cashly.cashly_api.receivables.reporting.application.dto.model.AccountsReceivableReportData.AccountReceivableRow;
import com.cashly.cashly_api.receivables.reporting.application.ports.out.AccountsReceivableReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateAccountsReceivableReportPdfUseCaseImpl Unit Tests")
class GenerateAccountsReceivableReportPdfUseCaseImplTest {

    @Mock
    private AccountsReceivableQueryGateway receivablesRepository;

    @Mock
    private AccountsReceivableReportPdfPort pdfPort;

    @InjectMocks
    private GenerateAccountsReceivableReportPdfUseCaseImpl useCase;

    private AccountReceivableRow mockRow(String customerIdHex, String name, String rif, int creditLimit, int pending, int available) {
        var id = java.util.UUID.randomUUID();
        return new AccountReceivableRow(
                id, name, rif,
                java.math.BigDecimal.valueOf(creditLimit),
                java.math.BigDecimal.valueOf(pending),
                java.math.BigDecimal.valueOf(available),
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO
        );
    }

    @Test
    @DisplayName("should generate accounts receivable PDF for customer")
    void should_generate_accounts_receivable_pdf() {
        UUID customerId = UUID.randomUUID();
        byte[] expectedPdf = "AR PDF".getBytes();

        when(receivablesRepository.findAccountsReceivableForReport(customerId, false))
                .thenReturn(List.<AccountReceivableRow>of(mockRow(null, "Test Customer", "J-123", 10000, 5000, 5000)));
        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(customerId, false);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }

    @Test
    @DisplayName("should generate accounts receivable PDF with overdue filter")
    void should_generate_overdue_accounts_receivable_pdf() {
        byte[] expectedPdf = "Overdue PDF".getBytes();

        when(receivablesRepository.findAccountsReceivableForReport(null, true))
                .thenReturn(List.<AccountReceivableRow>of(mockRow(null, "Overdue Customer", "J-456", 5000, 6000, 0)));
        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(null, true);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }

    @Test
    @DisplayName("should generate accounts receivable PDF without filters")
    void should_generate_accounts_receivable_pdf_without_filters() {
        byte[] expectedPdf = "All AR PDF".getBytes();

        when(receivablesRepository.findAccountsReceivableForReport(null, false))
                .thenReturn(List.<AccountReceivableRow>of(
                        mockRow(null, "Customer A", "J-111", 10000, 3000, 7000),
                        mockRow(null, "Customer B", "J-222", 8000, 9000, 0)
                ));
        when(pdfPort.generateReport(any())).thenReturn(expectedPdf);

        byte[] result = useCase.execute(null, false);

        assertArrayEquals(expectedPdf, result);
        verify(pdfPort).generateReport(any());
    }
}
