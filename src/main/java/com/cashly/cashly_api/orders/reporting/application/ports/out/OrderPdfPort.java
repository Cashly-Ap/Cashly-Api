package com.cashly.cashly_api.orders.reporting.application.ports.out;

import com.cashly.cashly_api.orders.reporting.application.dto.model.OrderReportData;


public interface OrderPdfPort {
    byte[] generatePdf(OrderReportData order);
}