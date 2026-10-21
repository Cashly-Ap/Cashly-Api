package com.cashly.cashly_api.finance.treasury.payments.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out.PaymentReportQueryGateway;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out.PaymentHistoryReportPdfPort;
import com.cashly.cashly_api.finance.treasury.payments.reporting.application.ports.out.PaymentPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PaymentReportingConfigTest {

    @Mock
    private PaymentReportQueryGateway getPaymentByIdUseCase;

    @Mock
    private PaymentPdfPort paymentPdfPort;

    @Mock
    private PaymentHistoryReportPdfPort paymentHistoryReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new PaymentReportingConfig();

        var paymentPdfUseCase = config.generatePaymentPdfUseCase(getPaymentByIdUseCase, paymentPdfPort);
        assertNotNull(paymentPdfUseCase);


        var phUseCase = config.generatePaymentHistoryReportPdfUseCase(paymentHistoryReportPdfPort);
        assertNotNull(phUseCase);

    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new PaymentReportingConfig();

        assertNotNull(config.paymentPdfPort(new PdfBranding()));
        assertNotNull(config.paymentHistoryReportPdfPort(new PdfBranding()));
    }
}
