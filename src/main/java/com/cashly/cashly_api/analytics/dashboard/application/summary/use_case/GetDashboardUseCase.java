package com.cashly.cashly_api.analytics.dashboard.application.summary.use_case;

import com.cashly.cashly_api.analytics.dashboard.application.ports.out.DashboardQueryGateway;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.DashboardSummaryResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.InvoiceDueResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.MonthlySalesResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.TopItemResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetDashboardUseCase {
    private final DashboardQueryGateway queryGateway;

    public GetDashboardUseCase(DashboardQueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    public DashboardSummaryResponse getSummary() {
        return queryGateway.getSummary();
    }

    public List<MonthlySalesResponse> getMonthlySales() {
        return queryGateway.getMonthlySales();
    }

    public List<TopItemResponse> getTopProducts(int limit) {
        return queryGateway.getTopProducts(limit);
    }

    public List<TopItemResponse> getTopCustomers(int limit) {
        return queryGateway.getTopCustomers(limit);
    }

    public List<Map<String, Object>> getRecentActivity() {
        return queryGateway.getRecentActivity();
    }

    public List<InvoiceDueResponse> getInvoiceDueSchedule() {
        return queryGateway.getInvoiceDueSchedule();
    }
}
