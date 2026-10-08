package com.cashly.cashly_api.reports.finance.application.port;

import com.cashly.cashly_api.reports.finance.application.dto.PaymentReportData;

public interface PaymentPdfPort {
    byte[] generatePaymentPdf(PaymentReportData payment);
}
