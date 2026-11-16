package com.cashly.cashly_api.analytics.dashboard.infrastructure.web;

import com.cashly.cashly_api.analytics.dashboard.application.dto.response.DashboardSummaryResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.InvoiceDueResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.MonthlySalesResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.ProfitSummaryResponse;
import com.cashly.cashly_api.analytics.dashboard.application.dto.response.TopItemResponse;
import com.cashly.cashly_api.analytics.dashboard.application.summary.use_case.GetDashboardUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final GetDashboardUseCase getDashboardUseCase;

    public DashboardController(GetDashboardUseCase getDashboardUseCase) {
        this.getDashboardUseCase = getDashboardUseCase;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(getDashboardUseCase.getSummary());
    }

    @GetMapping("/monthly-sales")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<List<MonthlySalesResponse>> getMonthlySales(
            @RequestParam(defaultValue = "MONTH") String period) {
        return ResponseEntity.ok(getDashboardUseCase.getSalesTrend(period));
    }

    @GetMapping("/profit-summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ProfitSummaryResponse> getProfitSummary(
            @RequestParam(defaultValue = "MONTH") String period) {
        return ResponseEntity.ok(getDashboardUseCase.getProfitSummary(period));
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<List<TopItemResponse>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(getDashboardUseCase.getTopProducts(limit));
    }

    @GetMapping("/top-customers")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<List<TopItemResponse>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(getDashboardUseCase.getTopCustomers(limit));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        return ResponseEntity.ok(getDashboardUseCase.getRecentActivity());
    }

    @GetMapping("/invoice-due-schedule")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<List<InvoiceDueResponse>> getInvoiceDueSchedule(
            @RequestParam(defaultValue = "ALL") String scope) {
        return ResponseEntity.ok(getDashboardUseCase.getInvoiceDueSchedule(scope));
    }
}
