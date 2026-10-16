package com.cashly.cashly_api.payments.reporting.infrastructure.config;

import com.cashly.cashly_api.payments.reporting.application.ports.out.PaymentReportQueryGateway;
import com.cashly.cashly_api.payments.reporting.application.ports.out.PaymentHistoryReportPdfPort;
import com.cashly.cashly_api.payments.reporting.application.ports.out.PaymentPdfPort;
import com.cashly.cashly_api.payments.reporting.application.generate_payment_history_report_pdf.GeneratePaymentHistoryReportPdfUseCase;
import com.cashly.cashly_api.payments.reporting.application.generate_payment_history_report_pdf.GeneratePaymentHistoryReportPdfUseCaseImpl;
import com.cashly.cashly_api.payments.reporting.application.generate_payment_pdf.GeneratePaymentPdfUseCase;
import com.cashly.cashly_api.payments.reporting.application.generate_payment_pdf.GeneratePaymentPdfUseCaseImpl;
import com.cashly.cashly_api.payments.reporting.infrastructure.pdf.PaymentHistoryReportPdfGenerator;
import com.cashly.cashly_api.payments.reporting.infrastructure.pdf.PaymentPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentReportingConfig {

    @Bean
    public PaymentPdfPort paymentPdfPort() {
        return new PaymentPdfGenerator();
    }

    @Bean
    public PaymentHistoryReportPdfPort paymentHistoryReportPdfPort() {
        return new PaymentHistoryReportPdfGenerator();
    }

    @Bean
    public GeneratePaymentPdfUseCase generatePaymentPdfUseCase(
            PaymentReportQueryGateway paymentQuery,
            PaymentPdfPort paymentPdfPort) {
        return new GeneratePaymentPdfUseCaseImpl(
                paymentQuery,
                paymentPdfPort
        );
    }

    @Bean
    public GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase(
            PaymentHistoryReportPdfPort pdfPort) {
        return new GeneratePaymentHistoryReportPdfUseCaseImpl(pdfPort);
    }
}
