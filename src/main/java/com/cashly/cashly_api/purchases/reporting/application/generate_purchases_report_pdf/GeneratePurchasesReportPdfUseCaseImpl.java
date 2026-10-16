package com.cashly.cashly_api.purchases.reporting.application.generate_purchases_report_pdf;

import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportFilter;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchasesReportPdfPort;



public class GeneratePurchasesReportPdfUseCaseImpl implements GeneratePurchasesReportPdfUseCase {

    private final PurchaseReportQueryGateway query;
    private final PurchasesReportPdfPort purchasesReportPdfPort;

    public GeneratePurchasesReportPdfUseCaseImpl(PurchaseReportQueryGateway query, PurchasesReportPdfPort purchasesReportPdfPort) {
        this.query = query;
        this.purchasesReportPdfPort = purchasesReportPdfPort;
    }

    @Override
    public byte[] execute(PurchaseReportFilter filter, String mode) {
        return purchasesReportPdfPort.generatePdf(query.findAll(filter), mode != null ? mode : "simple");
    }
}
