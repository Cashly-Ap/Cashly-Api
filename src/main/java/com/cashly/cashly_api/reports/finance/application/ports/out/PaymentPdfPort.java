package com.cashly.cashly_api.reports.finance.application.ports.out;

import com.cashly.cashly_api.reports.finance.application.dto.PaymentReportData;

public interface PaymentPdfPort {
    byte[] generatePaymentPdf(PaymentReportData payment);
}
