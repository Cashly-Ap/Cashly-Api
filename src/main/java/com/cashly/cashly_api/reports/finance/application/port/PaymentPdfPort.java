package com.cashly.cashly_api.reports.finance.application.port;

import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;

public interface PaymentPdfPort {
    byte[] generatePaymentPdf(PaymentResponse payment);
}
