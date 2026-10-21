package com.cashly.cashly_api.procurement.purchases.reporting.infrastructure.config;

import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasePdfPort;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasesReportPdfPort;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.use_case.GeneratePurchasePdfUseCase;
import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.use_case.GeneratePurchasesReportPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchasesReportConfig Unit Tests")
class PurchasesReportConfigTest {

    @Mock
    private PurchaseReportQueryGateway query;

    @Mock
    private PurchasePdfPort purchasePdfPort;

    @Mock
    private PurchasesReportPdfPort purchasesReportPdfPort;

    @Test
    @DisplayName("should create use case beans")
    void should_create_use_case_beans() {
        var config = new PurchasesReportConfig();

        var purchasePdfUseCase = config.generatePurchasePdfUseCase(query, purchasePdfPort);
        assertNotNull(purchasePdfUseCase);

        var reportPdfUseCase = config.generatePurchasesReportPdfUseCase(query, purchasesReportPdfPort);
        assertNotNull(reportPdfUseCase);
    }

    @Test
    @DisplayName("should create port beans")
    void should_create_port_beans() {
        var config = new PurchasesReportConfig();

        assertNotNull(config.purchasePdfPort(new PdfBranding()));
        assertNotNull(config.purchasesReportPdfPort(new PdfBranding()));
    }
}
