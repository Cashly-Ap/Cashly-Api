package com.cashly.cashly_api.reports.purchases.application.port;

import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;


public interface PurchasePdfPort {
    byte[] generatePdf(PurchaseDetailResponse purchase);
}
