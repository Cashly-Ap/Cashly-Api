package com.cashly.cashly_api.reports.orders.application.usecases;

import java.util.UUID;


public interface GenerateOrderPdfUseCase {
    byte[] execute(UUID orderId);
}