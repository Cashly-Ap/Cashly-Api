package com.cashly.cashly_api.reports.orders.application.port;

import com.cashly.cashly_api.reports.orders.application.dto.OrderReportRow;

import java.util.List;


public interface OrdersReportPdfPort {
    byte[] generatePdf(List<OrderReportRow> orders, String mode);
}