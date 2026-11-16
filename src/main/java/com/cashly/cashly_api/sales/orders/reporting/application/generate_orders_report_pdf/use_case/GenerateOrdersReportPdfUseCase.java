package com.cashly.cashly_api.sales.orders.reporting.application.generate_orders_report_pdf.use_case;

import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportFilter;

public interface GenerateOrdersReportPdfUseCase {
    byte[] execute(OrderReportFilter filter, String mode);
}