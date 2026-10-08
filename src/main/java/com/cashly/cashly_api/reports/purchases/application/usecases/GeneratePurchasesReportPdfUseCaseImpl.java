package com.cashly.cashly_api.reports.purchases.application.usecases;

import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportFilter;
import com.cashly.cashly_api.reports.purchases.application.contracts.PurchaseReportQueryGateway;
import com.cashly.cashly_api.reports.purchases.application.port.PurchasesReportPdfPort;



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
