package com.parsun.parsun_api.reports.purchases.application.usecases;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseListResponse;

import java.util.List;


public interface GeneratePurchasesReportPdfUseCase {
    byte[] execute(List<PurchaseListResponse> purchases, String mode);
}
