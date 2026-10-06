package com.cashly.cashly_api.reports.orders.application.port;

import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;

import java.util.List;


public interface OrdersReportPdfPort {
    byte[] generatePdf(List<OrderListResponse> orders, String mode);
}