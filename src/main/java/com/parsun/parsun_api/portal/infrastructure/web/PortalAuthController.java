package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.parsun.parsun_api.auth.token.application.dto.request.RefreshTokenRequest;
import com.parsun.parsun_api.auth.token.application.usecases.RefreshTokenUseCase;
import com.parsun.parsun_api.portal.application.dto.request.PortalLoginRequest;
import com.parsun.parsun_api.portal.application.dto.response.PortalAuthResponse;
import com.parsun.parsun_api.portal.application.usecases.PortalLoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/auth")
public class PortalAuthController {

    private final PortalLoginUseCase portalLoginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public PortalAuthController(PortalLoginUseCase portalLoginUseCase,
                                 RefreshTokenUseCase refreshTokenUseCase) {
        this.portalLoginUseCase = portalLoginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
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
}
