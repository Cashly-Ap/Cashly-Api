package com.cashly.cashly_api.dashboard.infrastructure.web;

import com.cashly.cashly_api.dashboard.application.dto.response.DashboardSummaryResponse;
import com.cashly.cashly_api.dashboard.application.dto.response.InvoiceDueResponse;
import com.cashly.cashly_api.dashboard.application.dto.response.MonthlySalesResponse;
import com.cashly.cashly_api.dashboard.application.dto.response.TopItemResponse;
import com.cashly.cashly_api.dashboard.application.summary.GetDashboardUseCase;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_DASHBOARD')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(getDashboardUseCase.getSummary());
    }

    @GetMapping("/monthly-sales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_DASHBOARD')")
    public ResponseEntity<List<MonthlySalesResponse>> getMonthlySales() {
        return ResponseEntity.ok(getDashboardUseCase.getMonthlySales());
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_DASHBOARD')")
    public ResponseEntity<List<TopItemResponse>> getTopProducts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(getDashboardUseCase.getTopProducts(limit));
    }

    @GetMapping("/top-customers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_DASHBOARD')")
    public ResponseEntity<List<TopItemResponse>> getTopCustomers(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(getDashboardUseCase.getTopCustomers(limit));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_DASHBOARD')")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        return ResponseEntity.ok(getDashboardUseCase.getRecentActivity());
    }

    @GetMapping("/invoice-due-schedule")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_DASHBOARD')")
    public ResponseEntity<List<InvoiceDueResponse>> getInvoiceDueSchedule() {
        return ResponseEntity.ok(getDashboardUseCase.getInvoiceDueSchedule());
    }
}
