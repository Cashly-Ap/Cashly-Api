package com.cashly.cashly_api.sales.orders.reporting.application.ports.out;

import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportData;
import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportRow;
import com.cashly.cashly_api.sales.orders.reporting.application.dto.model.OrderReportFilter;
import java.util.List;
import java.util.UUID;

public interface OrderReportQueryGateway {
    OrderReportData find(UUID id);
    List<OrderReportRow> findAll(OrderReportFilter filter);
}
