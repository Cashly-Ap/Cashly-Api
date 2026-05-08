package com.parsun.parsun_api.reports.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;

import java.util.List;

/**
 * Use case for generating PDF report of multiple orders.
 */
public interface GenerateOrdersReportPdfUseCase {
    byte[] execute(List<OrderListResponse> orders, String mode);
}