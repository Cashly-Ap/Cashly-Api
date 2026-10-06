package com.cashly.cashly_api.reports.orders.application.usecases;

import com.cashly.cashly_api.orders.application.dto.response.OrderListResponse;

import java.util.List;


public interface GenerateOrdersReportPdfUseCase {
    byte[] execute(List<OrderListResponse> orders, String mode);
}