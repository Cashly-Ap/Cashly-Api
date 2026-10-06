package com.cashly.cashly_api.reports.orders.application.port;

import com.cashly.cashly_api.orders.application.dto.response.OrderDetailResponse;


public interface OrderPdfPort {
    byte[] generatePdf(OrderDetailResponse order);
}