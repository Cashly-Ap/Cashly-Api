package com.cashly.cashly_api.payments.reporting.application.ports.out;

import com.cashly.cashly_api.payments.reporting.application.dto.model.PaymentReportData;

public interface PaymentPdfPort {
    byte[] generatePaymentPdf(PaymentReportData payment);
}
