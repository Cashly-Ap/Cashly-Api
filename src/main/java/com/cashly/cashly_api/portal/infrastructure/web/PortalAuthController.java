package com.cashly.cashly_api.portal.infrastructure.web;

import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.token.application.dto.request.LogoutRequest;
import com.cashly.cashly_api.auth.token.application.dto.request.RefreshTokenRequest;
import com.cashly.cashly_api.auth.token.application.usecases.LogoutUserUseCase;
import com.cashly.cashly_api.auth.token.application.usecases.RefreshTokenUseCase;
import com.cashly.cashly_api.portal.application.dto.request.ChangeCustomerPasswordRequest;
import com.cashly.cashly_api.portal.application.dto.request.PortalLoginRequest;
import com.cashly.cashly_api.portal.application.dto.response.CustomerSummaryResponse;
import com.cashly.cashly_api.portal.application.dto.response.PortalAuthResponse;
import com.cashly.cashly_api.portal.application.usecases.ChangeCustomerPasswordUseCase;
import com.cashly.cashly_api.portal.application.usecases.PortalLoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final PortalLoginUseCase portalLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ChangeCustomerPasswordUseCase changeCustomerPasswordUseCase;
    private final LogoutUserUseCase logoutUserUseCase;

    public PortalAuthController(PortalLoginUseCase portalLoginUseCase,
                                 RefreshTokenUseCase refreshTokenUseCase,
                                 ChangeCustomerPasswordUseCase changeCustomerPasswordUseCase,
                                 LogoutUserUseCase logoutUserUseCase) {
        this.portalLoginUseCase = portalLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.changeCustomerPasswordUseCase = changeCustomerPasswordUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<PortalAuthResponse> login(@Valid @RequestBody PortalLoginRequest request) {
        PortalAuthResponse response = portalLoginUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<CustomerSummaryResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangeCustomerPasswordRequest request) {
        String customerId = (String) authentication.getPrincipal();
        CustomerSummaryResponse response = changeCustomerPasswordUseCase.execute(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutUserUseCase.execute(request);
        return ResponseEntity.noContent().build();
    }
}
