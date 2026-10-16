package com.cashly.cashly_api.purchases.reporting.application.generate_purchase_pdf;

import java.util.UUID;


public interface GeneratePurchasePdfUseCase {
    byte[] execute(UUID purchaseId);
}
