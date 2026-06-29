package com.parsun.parsun_api.reports.purchases.application.port;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseListResponse;

import java.util.List;


public interface PurchasesReportPdfPort {
    byte[] generatePdf(List<PurchaseListResponse> purchases, String mode);
}
