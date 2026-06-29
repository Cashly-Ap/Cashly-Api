package com.parsun.parsun_api.reports.purchases.application.port;

import com.parsun.parsun_api.purchases.application.dto.response.PurchaseDetailResponse;


public interface PurchasePdfPort {
    byte[] generatePdf(PurchaseDetailResponse purchase);
}
