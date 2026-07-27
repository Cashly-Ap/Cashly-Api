package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.portal.application.dto.response.PortalDashboardResponse;
import com.parsun.parsun_api.portal.application.usecases.GetPortalDashboardUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/dashboard")
public class PortalDashboardController {

    private final GetPortalDashboardUseCase getPortalDashboardUseCase;

    public PortalDashboardController(GetPortalDashboardUseCase getPortalDashboardUseCase) {
        this.getPortalDashboardUseCase = getPortalDashboardUseCase;
    }

    @GetMapping
    public ResponseEntity<PortalDashboardResponse> getDashboard() {
        PortalDashboardResponse response = getPortalDashboardUseCase.execute();
        return ResponseEntity.ok(response);
    }
}
