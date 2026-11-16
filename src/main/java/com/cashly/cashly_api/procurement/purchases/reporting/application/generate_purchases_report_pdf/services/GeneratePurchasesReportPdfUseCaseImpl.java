package com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.services;

import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchases_report_pdf.use_case.GeneratePurchasesReportPdfUseCase;

import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportFilter;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasesReportPdfPort;

public class GeneratePurchasesReportPdfUseCaseImpl
        implements
            GeneratePurchasesReportPdfUseCase {

    private final PurchaseReportQueryGateway query;
    private final PurchasesReportPdfPort purchasesReportPdfPort;

    public GeneratePurchasesReportPdfUseCaseImpl(
            PurchaseReportQueryGateway query,
            PurchasesReportPdfPort purchasesReportPdfPort) {
        this.query = query;
        this.purchasesReportPdfPort = purchasesReportPdfPort;
    }

    @Override
    public byte[] execute(PurchaseReportFilter filter, String mode) {
        return purchasesReportPdfPort.generatePdf(query.findAll(filter),
                mode != null ? mode : "simple");
    }
}
