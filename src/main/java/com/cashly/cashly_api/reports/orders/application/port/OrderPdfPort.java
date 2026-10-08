package com.cashly.cashly_api.reports.orders.application.port;

import com.cashly.cashly_api.reports.orders.application.dto.OrderReportData;


public interface OrderPdfPort {
    byte[] generatePdf(OrderReportData order);
}