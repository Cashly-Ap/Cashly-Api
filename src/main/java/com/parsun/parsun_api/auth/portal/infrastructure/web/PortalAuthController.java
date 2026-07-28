package com.parsun.parsun_api.auth.portal.infrastructure.web;

import com.parsun.parsun_api.auth.portal.application.dto.request.PortalLoginRequest;
import com.parsun.parsun_api.auth.portal.application.dto.response.PortalAuthResponse;
import com.parsun.parsun_api.auth.portal.application.usecases.LoginCustomerUseCase;
import com.parsun.parsun_api.auth.portal.application.usecases.PortalRefreshTokenUseCase;
import com.parsun.parsun_api.auth.token.application.dto.request.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final LoginCustomerUseCase loginCustomerUseCase;
    private final PortalRefreshTokenUseCase portalRefreshTokenUseCase;

    public PortalAuthController(LoginCustomerUseCase loginCustomerUseCase,
                                PortalRefreshTokenUseCase portalRefreshTokenUseCase) {
        this.loginCustomerUseCase = loginCustomerUseCase;
        this.portalRefreshTokenUseCase = portalRefreshTokenUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<PortalAuthResponse> login(@Valid @RequestBody PortalLoginRequest request) {
        PortalAuthResponse response = loginCustomerUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<PortalAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        PortalAuthResponse response = portalRefreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
