package com.cashly.cashly_api.payments.reporting.application.generate_payment_pdf.services;

import com.cashly.cashly_api.payments.reporting.application.dto.model.PaymentReportData;
import com.cashly.cashly_api.payments.reporting.application.ports.out.PaymentReportQueryGateway;
import com.cashly.cashly_api.payments.reporting.application.ports.out.PaymentPdfPort;
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
@DisplayName("GeneratePaymentPdfUseCaseImpl Unit Tests")
class GeneratePaymentPdfUseCaseImplTest {

    @Mock
    private PaymentReportQueryGateway getPaymentByIdUseCase;

    @Mock
    private PaymentPdfPort paymentPdfPort;

    @InjectMocks
    private GeneratePaymentPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate payment PDF for given payment ID")
    void should_generate_payment_pdf() {
        UUID paymentId = UUID.randomUUID();
        PaymentReportData payment = mock(PaymentReportData.class);
        byte[] expectedPdf = "Payment PDF".getBytes();

        when(getPaymentByIdUseCase.find(paymentId)).thenReturn(payment);
        when(paymentPdfPort.generatePaymentPdf(payment)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(paymentId);

        assertArrayEquals(expectedPdf, result);
        verify(getPaymentByIdUseCase).find(paymentId);
        verify(paymentPdfPort).generatePaymentPdf(payment);
    }
}
