package com.parsun.parsun_api.reports.finance.application.port;

import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;

public interface PaymentPdfPort {
    byte[] generatePaymentPdf(PaymentResponse payment);
}
