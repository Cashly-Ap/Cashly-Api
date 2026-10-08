package com.cashly.cashly_api.reports.orders.application.contracts;

import com.cashly.cashly_api.reports.orders.application.dto.OrderReportData;
import com.cashly.cashly_api.reports.orders.application.dto.OrderReportRow;
import com.cashly.cashly_api.reports.orders.application.dto.OrderReportFilter;
import java.util.List;
import java.util.UUID;

public interface OrderReportQueryGateway {
    OrderReportData find(UUID id);
    List<OrderReportRow> findAll(OrderReportFilter filter);
}
