package com.cashly.cashly_api.purchases.reporting.application.ports.out;

import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportRow;

import java.util.List;


public interface PurchasesReportPdfPort {
    byte[] generatePdf(List<PurchaseReportRow> purchases, String mode);
}
