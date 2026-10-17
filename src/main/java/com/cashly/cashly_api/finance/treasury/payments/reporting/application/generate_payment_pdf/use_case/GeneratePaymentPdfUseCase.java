package com.cashly.cashly_api.finance.treasury.payments.reporting.application.generate_payment_pdf.use_case;

import java.util.UUID;

public interface GeneratePaymentPdfUseCase {
    byte[] execute(UUID paymentId);
}
