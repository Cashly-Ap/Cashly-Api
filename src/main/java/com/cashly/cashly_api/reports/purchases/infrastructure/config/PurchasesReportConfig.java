package com.cashly.cashly_api.reports.purchases.infrastructure.config;
import com.cashly.cashly_api.reports.purchases.application.contracts.PurchaseReportQueryGateway;
import com.cashly.cashly_api.reports.purchases.application.port.PurchasePdfPort;
import com.cashly.cashly_api.reports.purchases.application.port.PurchasesReportPdfPort;
import com.cashly.cashly_api.reports.purchases.application.usecases.GeneratePurchasePdfUseCase;
import com.cashly.cashly_api.reports.purchases.application.usecases.GeneratePurchasePdfUseCaseImpl;
import com.cashly.cashly_api.reports.purchases.application.usecases.GeneratePurchasesReportPdfUseCase;
import com.cashly.cashly_api.reports.purchases.application.usecases.GeneratePurchasesReportPdfUseCaseImpl;
import com.cashly.cashly_api.reports.purchases.infrastructure.pdf.PurchasePdfGenerator;
import com.cashly.cashly_api.reports.purchases.infrastructure.pdf.PurchasesReportPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PurchasesReportConfig {
    @Bean
    public PurchasePdfPort purchasePdfPort() { return new PurchasePdfGenerator(); }
    @Bean
    public PurchasesReportPdfPort purchasesReportPdfPort() { return new PurchasesReportPdfGenerator(); }
    @Bean
    public GeneratePurchasePdfUseCase generatePurchasePdfUseCase(PurchaseReportQueryGateway query, PurchasePdfPort pdf) {
        return new GeneratePurchasePdfUseCaseImpl(query, pdf);
    }
    @Bean
    public GeneratePurchasesReportPdfUseCase generatePurchasesReportPdfUseCase(PurchaseReportQueryGateway query, PurchasesReportPdfPort pdf) {
        return new GeneratePurchasesReportPdfUseCaseImpl(query, pdf);
    }
}