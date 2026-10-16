package com.cashly.cashly_api.orders.reporting.application.generate_order_pdf;

import java.util.UUID;


public interface GenerateOrderPdfUseCase {
    byte[] execute(UUID orderId);
}