package com.cashly.cashly_api.reports.purchases.application.usecases;

import com.cashly.cashly_api.reports.purchases.application.contracts.PurchaseReportQueryGateway;
import com.cashly.cashly_api.reports.purchases.application.port.PurchasePdfPort;
import java.util.UUID;

public class GeneratePurchasePdfUseCaseImpl implements GeneratePurchasePdfUseCase {
    private final PurchaseReportQueryGateway query;
    private final PurchasePdfPort pdf;
    public GeneratePurchasePdfUseCaseImpl(PurchaseReportQueryGateway query, PurchasePdfPort pdf) {
        this.query = query;
        this.pdf = pdf;
    }
    public byte[] execute(UUID id) { return pdf.generatePdf(query.find(id)); }
}
