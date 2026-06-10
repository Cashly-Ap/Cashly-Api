package com.parsun.parsun_api.reports.orders.application.port;

import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;


public interface OrderPdfPort {
    byte[] generatePdf(OrderDetailResponse order);
}