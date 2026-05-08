package com.parsun.parsun_api.reports.orders.application.usecases;

import java.util.UUID;

/**
 * Use case for generating PDF of a single order.
 */
public interface GenerateOrderPdfUseCase {
    byte[] execute(UUID orderId);
}