package com.cashly.cashly_api.reports.purchases.application.usecases;

import com.cashly.cashly_api.reports.purchases.application.dto.PurchaseReportFilter;



public interface GeneratePurchasesReportPdfUseCase {
    byte[] execute(PurchaseReportFilter filter, String mode);
}
