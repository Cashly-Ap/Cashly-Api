package com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.config;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasePdfPort;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasesReportPdfPort;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.use_case.GeneratePurchasePdfUseCase;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.services.GeneratePurchasePdfUseCaseImpl;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.use_case.GeneratePurchasesReportPdfUseCase;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.services.GeneratePurchasesReportPdfUseCaseImpl;
import com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.pdf.PurchasePdfGenerator;
import com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.pdf.PurchasesReportPdfGenerator;
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