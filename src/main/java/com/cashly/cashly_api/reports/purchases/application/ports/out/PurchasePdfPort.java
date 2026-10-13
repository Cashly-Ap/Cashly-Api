package com.cashly.cashly_api.reports.purchases.application.ports.out;

import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportData;


public interface PurchasePdfPort {
    byte[] generatePdf(PurchaseReportData purchase);
}
