package com.parsun.parsun_api.reports.finance.application.usecases;

import java.util.UUID;

public interface GeneratePaymentPdfUseCase {
    byte[] execute(UUID paymentId);
}
