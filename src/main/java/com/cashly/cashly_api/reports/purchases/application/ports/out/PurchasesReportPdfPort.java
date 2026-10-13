package com.cashly.cashly_api.reports.purchases.application.ports.out;

import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportRow;

import java.util.List;


public interface PurchasesReportPdfPort {
    byte[] generatePdf(List<PurchaseReportRow> purchases, String mode);
}
