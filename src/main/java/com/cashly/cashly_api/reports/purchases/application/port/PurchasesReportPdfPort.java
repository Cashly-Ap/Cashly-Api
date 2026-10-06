package com.cashly.cashly_api.reports.purchases.application.port;

import com.cashly.cashly_api.purchases.application.dto.response.PurchaseListResponse;

import java.util.List;


public interface PurchasesReportPdfPort {
    byte[] generatePdf(List<PurchaseListResponse> purchases, String mode);
}
