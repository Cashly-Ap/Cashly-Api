package com.parsun.parsun_api.reports.purchases.infrastructure.config;

import com.parsun.parsun_api.purchases.domain.repositories.PurchaseRepository;
import com.parsun.parsun_api.purchases.application.usecases.ListPurchasesUseCase;
import com.parsun.parsun_api.reports.purchases.application.port.PurchasePdfPort;
import com.parsun.parsun_api.reports.purchases.application.port.PurchasesReportPdfPort;
import com.parsun.parsun_api.reports.purchases.application.usecases.GeneratePurchasePdfUseCase;
import com.parsun.parsun_api.reports.purchases.application.usecases.GeneratePurchasePdfUseCaseImpl;
import com.parsun.parsun_api.reports.purchases.application.usecases.GeneratePurchasesReportPdfUseCase;
import com.parsun.parsun_api.reports.purchases.application.usecases.GeneratePurchasesReportPdfUseCaseImpl;
import com.parsun.parsun_api.reports.purchases.infrastructure.pdf.PurchasePdfGenerator;
import com.parsun.parsun_api.reports.purchases.infrastructure.pdf.PurchasesReportPdfGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PurchasesReportConfig {

    @Bean
    public PurchasePdfPort purchasePdfPort() {
        return new PurchasePdfGenerator();
    }

    @Bean
    public PurchasesReportPdfPort purchasesReportPdfPort() {
        return new PurchasesReportPdfGenerator();
    }

    @Bean
    public GeneratePurchasePdfUseCase generatePurchasePdfUseCase(
            PurchaseRepository purchaseRepository,
            PurchasePdfPort purchasePdfPort) {
        return new GeneratePurchasePdfUseCaseImpl(purchaseRepository, purchasePdfPort);
    }

    @Bean
    public GeneratePurchasesReportPdfUseCase generatePurchasesReportPdfUseCase(
            PurchasesReportPdfPort purchasesReportPdfPort) {
        return new GeneratePurchasesReportPdfUseCaseImpl(purchasesReportPdfPort);
    }
}
