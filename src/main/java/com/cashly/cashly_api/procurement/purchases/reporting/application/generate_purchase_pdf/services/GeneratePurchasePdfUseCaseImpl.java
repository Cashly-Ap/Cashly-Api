package com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.services;

import com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.use_case.GeneratePurchasePdfUseCase;

import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out.PurchasePdfPort;
import java.util.UUID;

public class GeneratePurchasePdfUseCaseImpl
        implements
            GeneratePurchasePdfUseCase {
    private final PurchaseReportQueryGateway query;
    private final PurchasePdfPort pdf;
    public GeneratePurchasePdfUseCaseImpl(PurchaseReportQueryGateway query,
            PurchasePdfPort pdf) {
        this.query = query;
        this.pdf = pdf;
    }
    public byte[] execute(UUID id) {
        return pdf.generatePdf(query.find(id));
    }
}
