package com.cashly.cashly_api.procurement.purchases.reporting.application.generate_purchase_pdf.use_case;

import java.util.UUID;

public interface GeneratePurchasePdfUseCase {
    byte[] execute(UUID purchaseId);
}
