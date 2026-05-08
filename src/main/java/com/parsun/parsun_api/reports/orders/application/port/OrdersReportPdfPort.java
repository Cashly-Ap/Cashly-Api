package com.parsun.parsun_api.reports.orders.application.port;

import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;

import java.util.List;

/**
 * Port for generating PDF report for multiple orders.
 */
public interface OrdersReportPdfPort {
    byte[] generatePdf(List<OrderListResponse> orders, String mode);
}