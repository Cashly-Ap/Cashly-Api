package com.cashly.cashly_api.orders.reporting.application.ports.out;

import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportData;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportRow;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportFilter;
import java.util.List;
import java.util.UUID;

public interface OrderReportQueryGateway {
    OrderReportData find(UUID id);
    List<OrderReportRow> findAll(OrderReportFilter filter);
}
