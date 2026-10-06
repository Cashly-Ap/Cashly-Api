package com.cashly.cashly_api.reports.purchases.application.usecases;

import com.cashly.cashly_api.purchases.application.dto.response.PurchaseListResponse;
import com.cashly.cashly_api.reports.purchases.application.port.PurchasesReportPdfPort;

import java.util.List;


public class GeneratePurchasesReportPdfUseCaseImpl implements GeneratePurchasesReportPdfUseCase {

    private final PurchasesReportPdfPort purchasesReportPdfPort;

    public GeneratePurchasesReportPdfUseCaseImpl(PurchasesReportPdfPort purchasesReportPdfPort) {
        this.purchasesReportPdfPort = purchasesReportPdfPort;
    }

    @Override
    public byte[] execute(List<PurchaseListResponse> purchases, String mode) {
        return purchasesReportPdfPort.generatePdf(purchases, mode != null ? mode : "simple");
    }
}
