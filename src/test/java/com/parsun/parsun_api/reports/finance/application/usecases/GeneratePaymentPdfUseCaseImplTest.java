package com.parsun.parsun_api.reports.finance.application.usecases;

import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.parsun.parsun_api.reports.finance.application.port.PaymentPdfPort;
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
    private GetPaymentByIdUseCase getPaymentByIdUseCase;

    @Mock
    private PaymentPdfPort paymentPdfPort;

    @InjectMocks
    private GeneratePaymentPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate payment PDF for given payment ID")
    void should_generate_payment_pdf() {
        UUID paymentId = UUID.randomUUID();
        PaymentResponse payment = mock(PaymentResponse.class);
        byte[] expectedPdf = "Payment PDF".getBytes();

        when(getPaymentByIdUseCase.execute(paymentId)).thenReturn(payment);
        when(paymentPdfPort.generatePaymentPdf(payment)).thenReturn(expectedPdf);

        byte[] result = useCase.execute(paymentId);

        assertArrayEquals(expectedPdf, result);
        verify(getPaymentByIdUseCase).execute(paymentId);
        verify(paymentPdfPort).generatePaymentPdf(payment);
    }
}
