package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.customers.domain.valueobjects.Rif;
import com.parsun.parsun_api.portal.application.dto.request.PortalLoginRequest;
import com.parsun.parsun_api.portal.application.dto.response.CustomerSummaryResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalAuthResponse;
import org.springframework.transaction.annotation.Transactional;

public class PortalLoginUseCase {

    private final AuthenticableResolver customerRifResolver;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PortalLoginUseCase(
            AuthenticableResolver customerRifResolver,
            RefreshTokenRepository refreshTokenRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder) {
        this.customerRifResolver = customerRifResolver;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PortalAuthResponse execute(PortalLoginRequest request) {
        Rif rif;
        try {
            rif = new Rif(request.rif());
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid RIF format");
        }

        var authenticable = customerRifResolver.resolveByRif(rif)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid RIF or password"));

        if (!authenticable.canAuthenticate()) {
            throw new InvalidCredentialsException("Account is not active or portal access is disabled");
        }

        if (!passwordEncoder.matches(request.password(), authenticable.getHashedPassword())) {
            throw new InvalidCredentialsException("Invalid RIF or password");
        }

        String accessToken = tokenService.generateAccessToken(authenticable);
        RefreshToken refreshToken = tokenService.generateRefreshToken(authenticable);
        refreshTokenRepository.save(refreshToken);

        var customer = new CustomerSummaryResponse(
                java.util.UUID.fromString(authenticable.getSubjectId()),
                authenticable.getUserName(),
                rif.getValue(),
                null,
                null,
                null
        );

        return new PortalAuthResponse(
                accessToken,
                refreshToken.getToken(),
                tokenService.getAccessTokenExpiration(),
                customer
        );
    }
}
