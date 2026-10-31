package com.cashly.cashly_api.sales.orders.reporting.application.ports.out;

import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportRow;

import java.util.List;

public interface OrdersReportPdfPort {
    byte[] generatePdf(List<OrderReportRow> orders, String mode);
}