package com.parsun.parsun_api.reports.finances.infrastructure.config;

import com.parsun.parsun_api.finances.application.usecases.GetPaymentByIdUseCase;
import com.parsun.parsun_api.reports.finances.application.port.AccountsReceivableReportPdfPort;
import com.parsun.parsun_api.reports.finances.application.port.CollectionsReportPdfPort;
import com.parsun.parsun_api.reports.finances.application.port.PaymentHistoryReportPdfPort;
import com.parsun.parsun_api.reports.finances.application.port.PaymentPdfPort;
import com.parsun.parsun_api.reports.finances.application.usecases.GenerateAccountsReceivableReportPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GenerateAccountsReceivableReportPdfUseCaseImpl;
import com.parsun.parsun_api.reports.finances.application.usecases.GenerateCollectionsReportPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GenerateCollectionsReportPdfUseCaseImpl;
import com.parsun.parsun_api.reports.finances.application.usecases.GeneratePaymentHistoryReportPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GeneratePaymentHistoryReportPdfUseCaseImpl;
import com.parsun.parsun_api.reports.finances.application.usecases.GeneratePaymentPdfUseCase;
import com.parsun.parsun_api.reports.finances.application.usecases.GeneratePaymentPdfUseCaseImpl;
import com.parsun.parsun_api.reports.finances.infrastructure.pdf.AccountsReceivableReportPdfGenerator;
import com.parsun.parsun_api.reports.finances.infrastructure.pdf.CollectionsReportPdfGenerator;
import com.parsun.parsun_api.reports.finances.infrastructure.pdf.PaymentHistoryReportPdfGenerator;
import com.parsun.parsun_api.reports.finances.infrastructure.pdf.PaymentPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinancesReportConfig {

    @Bean
    public PaymentPdfPort paymentPdfPort() {
        return new PaymentPdfGenerator();
    }

    @Bean
    public AccountsReceivableReportPdfPort accountsReceivableReportPdfPort() {
        return new AccountsReceivableReportPdfGenerator();
    }

    @Bean
    public PaymentHistoryReportPdfPort paymentHistoryReportPdfPort() {
        return new PaymentHistoryReportPdfGenerator();
    }

    @Bean
    public CollectionsReportPdfPort collectionsReportPdfPort() {
        return new CollectionsReportPdfGenerator();
    }

    @Bean
    public GeneratePaymentPdfUseCase generatePaymentPdfUseCase(
            GetPaymentByIdUseCase getPaymentByIdUseCase,
            PaymentPdfPort paymentPdfPort) {
        return new GeneratePaymentPdfUseCaseImpl(
                getPaymentByIdUseCase,
                paymentPdfPort
        );
    }

    @Bean
    public GenerateAccountsReceivableReportPdfUseCase generateAccountsReceivableReportPdfUseCase(
            AccountsReceivableReportPdfPort pdfPort) {
        return new GenerateAccountsReceivableReportPdfUseCaseImpl(pdfPort);
    }

    @Bean
    public GeneratePaymentHistoryReportPdfUseCase generatePaymentHistoryReportPdfUseCase(
            PaymentHistoryReportPdfPort pdfPort) {
        return new GeneratePaymentHistoryReportPdfUseCaseImpl(pdfPort);
    }

    @Bean
    public GenerateCollectionsReportPdfUseCase generateCollectionsReportPdfUseCase(
            CollectionsReportPdfPort pdfPort) {
        return new GenerateCollectionsReportPdfUseCaseImpl(pdfPort);
    }
}
