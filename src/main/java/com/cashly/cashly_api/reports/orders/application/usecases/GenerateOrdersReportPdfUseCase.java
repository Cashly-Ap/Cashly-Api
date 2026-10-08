package com.cashly.cashly_api.reports.orders.application.usecases;

import com.cashly.cashly_api.reports.orders.application.dto.OrderReportFilter;



public interface GenerateOrdersReportPdfUseCase {
    byte[] execute(OrderReportFilter filter, String mode);
}