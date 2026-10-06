package com.cashly.cashly_api.reports.finance.infrastructure.config;

import com.cashly.cashly_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.cashly.cashly_api.receivables.domain.repositories.ReceivablesRepository;
import com.cashly.cashly_api.reports.finance.application.port.AccountsReceivableReportPdfPort;
import com.cashly.cashly_api.reports.finance.application.port.CollectionsReportPdfPort;
import com.cashly.cashly_api.reports.finance.application.port.PaymentHistoryReportPdfPort;
import com.cashly.cashly_api.reports.finance.application.port.PaymentPdfPort;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateAccountsReceivableReportPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateAccountsReceivableReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateCollectionsReportPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GenerateCollectionsReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentHistoryReportPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentHistoryReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentPdfUseCase;
import com.cashly.cashly_api.reports.finance.application.usecases.GeneratePaymentPdfUseCaseImpl;
import com.cashly.cashly_api.reports.finance.infrastructure.pdf.AccountsReceivableReportPdfGenerator;
import com.cashly.cashly_api.reports.finance.infrastructure.pdf.CollectionsReportPdfGenerator;
import com.cashly.cashly_api.reports.finance.infrastructure.pdf.PaymentHistoryReportPdfGenerator;
import com.cashly.cashly_api.reports.finance.infrastructure.pdf.PaymentPdfGenerator;
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
            ReceivablesRepository receivablesRepository,
            AccountsReceivableReportPdfPort pdfPort) {
        return new GenerateAccountsReceivableReportPdfUseCaseImpl(receivablesRepository, pdfPort);
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
