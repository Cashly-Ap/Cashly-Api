package com.cashly.cashly_api.portal.infrastructure.web;

import com.cashly.cashly_api.portal.application.dto.response.PortalProfileResponse;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import com.cashly.cashly_api.portal.application.usecases.GetCustomerProfileUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/portal/profile")
public class PortalProfileController {

    private final GetCustomerProfileUseCase getCustomerProfileUseCase;
    private final PortalAuthorizationService portalAuthorizationService;

    public PortalProfileController(GetCustomerProfileUseCase getCustomerProfileUseCase,
                                    PortalAuthorizationService portalAuthorizationService) {
        this.getCustomerProfileUseCase = getCustomerProfileUseCase;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<PortalProfileResponse> getProfile() {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        PortalProfileResponse response = getCustomerProfileUseCase.execute(customerId);
        return ResponseEntity.ok(response);
    }
}
