package com.cashly.cashly_api.sales.orders.reporting.application.generate_order_pdf.use_case;

import java.util.UUID;

public interface GenerateOrderPdfUseCase {
    byte[] execute(UUID orderId);
}