package com.cashly.cashly_api.reports.invoices.application.usecases;

import com.cashly.cashly_api.reports.invoices.application.dto.InvoiceReportData;
import com.cashly.cashly_api.reports.invoices.application.contracts.InvoiceReportQueryGateway;
import com.cashly.cashly_api.reports.invoices.application.port.InvoicePdfPort;
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
@DisplayName("GenerateInvoicePdfUseCaseImpl Unit Tests")
class GenerateInvoicePdfUseCaseImplTest {

    @Mock
    private InvoiceReportQueryGateway getInvoiceByIdUseCase;

    @Mock
    private InvoicePdfPort invoicePdfPort;

    @InjectMocks
    private GenerateInvoicePdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate invoice PDF for given invoice ID")
    void should_generate_invoice_pdf() {
        UUID invoiceId = UUID.randomUUID();
        InvoiceReportData invoice = mock(InvoiceReportData.class);
        byte[] expectedPdf = "Invoice PDF".getBytes();

        when(getInvoiceByIdUseCase.find(invoiceId)).thenReturn(invoice);
        when(invoicePdfPort.generatePdf(invoice)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(invoiceId);

        assertArrayEquals(expectedPdf, result);
        verify(getInvoiceByIdUseCase).find(invoiceId);
        verify(invoicePdfPort).generatePdf(invoice);
    }
}
