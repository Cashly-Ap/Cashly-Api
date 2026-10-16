package com.cashly.cashly_api.purchases.reporting.application.generate_purchases_report_pdf;

import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportFilter;



public interface GeneratePurchasesReportPdfUseCase {
    byte[] execute(PurchaseReportFilter filter, String mode);
}
