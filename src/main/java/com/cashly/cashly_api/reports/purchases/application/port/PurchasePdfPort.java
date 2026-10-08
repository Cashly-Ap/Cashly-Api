package com.cashly.cashly_api.reports.purchases.application.port;

import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportData;


public interface PurchasePdfPort {
    byte[] generatePdf(PurchaseReportData purchase);
}
