package com.parsun.parsun_api.reports.purchases.application.usecases;

import java.util.UUID;


public interface GeneratePurchasePdfUseCase {
    byte[] execute(UUID purchaseId);
}
