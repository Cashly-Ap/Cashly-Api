package com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf;

import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportFilter;



public interface GenerateOrdersReportPdfUseCase {
    byte[] execute(OrderReportFilter filter, String mode);
}