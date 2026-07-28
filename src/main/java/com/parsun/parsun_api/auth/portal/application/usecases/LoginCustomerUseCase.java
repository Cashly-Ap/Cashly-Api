package com.parsun.parsun_api.auth.portal.application.usecases;

import com.parsun.parsun_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.services.AuthenticableResolver;
import com.parsun.parsun_api.auth.portal.application.dto.request.PortalLoginRequest;
import com.parsun.parsun_api.auth.portal.application.dto.response.PortalAuthResponse;
import com.parsun.parsun_api.auth.portal.application.dto.response.PortalCustomerResponse;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class LoginCustomerUseCase {

    private final AuthenticableResolver authenticableResolver;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public LoginCustomerUseCase(
        AuthenticableResolver authenticableResolver,
        RefreshTokenRepository refreshTokenRepository,
        TokenService tokenService,
        PasswordEncoder passwordEncoder
    ) {
        this.authenticableResolver = authenticableResolver;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public PortalAuthResponse execute(PortalLoginRequest request) {
        Authenticable authenticable = authenticableResolver.resolveByRif(request.rif())
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

        PortalCustomerResponse customer = new PortalCustomerResponse(
            authenticable.getUserName(),
            request.rif()
        );

        return PortalAuthResponse.of(
            accessToken,
            refreshToken.getToken(),
            tokenService.getAccessTokenExpiration(),
            customer
        );
    }
}
