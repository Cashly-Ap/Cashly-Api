package com.cashly.cashly_api.purchases.reporting.application.ports.out;

import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportData;


public interface PurchasePdfPort {
    byte[] generatePdf(PurchaseReportData purchase);
}
