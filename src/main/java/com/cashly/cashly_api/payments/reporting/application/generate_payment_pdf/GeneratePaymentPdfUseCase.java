package com.cashly.cashly_api.payments.reporting.application.generate_payment_pdf;

import java.util.UUID;

public interface GeneratePaymentPdfUseCase {
    byte[] execute(UUID paymentId);
}
