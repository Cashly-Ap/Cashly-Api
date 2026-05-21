package com.parsun.parsun_api.reports.finances.application.port;

import com.parsun.parsun_api.finances.application.dto.response.PaymentDetailResponse;

public interface PaymentPdfPort {
    byte[] generatePaymentPdf(PaymentDetailResponse payment);
}
