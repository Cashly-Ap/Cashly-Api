package com.cashly.cashly_api.dashboard.application.ports.out;

import com.cashly.cashly_api.dashboard.application.dto.response.DashboardSummaryResponse;
import com.cashly.cashly_api.dashboard.application.dto.response.InvoiceDueResponse;
import com.cashly.cashly_api.dashboard.application.dto.response.MonthlySalesResponse;
import com.cashly.cashly_api.dashboard.application.dto.response.TopItemResponse;

import java.util.List;
import java.util.Map;

public interface DashboardQueryGateway {
    DashboardSummaryResponse getSummary();
    List<MonthlySalesResponse> getMonthlySales();
    List<TopItemResponse> getTopProducts(int limit);
    List<TopItemResponse> getTopCustomers(int limit);
    List<Map<String, Object>> getRecentActivity();
    List<InvoiceDueResponse> getInvoiceDueSchedule();
}
