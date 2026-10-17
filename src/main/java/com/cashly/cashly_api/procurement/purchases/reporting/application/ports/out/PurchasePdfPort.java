package com.cashly.cashly_api.procurement.purchases.reporting.application.ports.out;

import com.cashly.cashly_api.procurement.purchases.reporting.application.dto.model.PurchaseReportData;


public interface PurchasePdfPort {
    byte[] generatePdf(PurchaseReportData purchase);
}
