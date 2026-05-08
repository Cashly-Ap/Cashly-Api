package com.parsun.parsun_api.reports.orders.application.port;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;

/**
 * Port for generating PDF for a single order.
 */
public interface OrderPdfPort {
    byte[] generatePdf(OrderDetailResponse order);
}