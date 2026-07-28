package com.parsun.parsun_api.auth.portal.application.usecases;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.authentication.infrastructure.adapters.CustomerAuthenticableAdapter;
import com.parsun.parsun_api.auth.portal.application.dto.response.PortalAuthResponse;
import com.parsun.parsun_api.auth.portal.application.dto.response.PortalCustomerResponse;
import com.parsun.parsun_api.auth.token.application.dto.request.RefreshTokenRequest;
import com.parsun.parsun_api.auth.token.application.exceptions.InvalidTokenException;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import org.springframework.transaction.annotation.Transactional;

public class PortalRefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomerRepository customerRepository;
    private final TokenService tokenService;

    public PortalRefreshTokenUseCase(
        RefreshTokenRepository refreshTokenRepository,
        CustomerRepository customerRepository,
        TokenService tokenService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.customerRepository = customerRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public PortalAuthResponse execute(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        if (refreshToken.getSubjectType() != AuthenticableType.CUSTOMER) {
            throw new InvalidTokenException("Invalid token type");
        }

        Customer customer = customerRepository.findById(new CustomerId(java.util.UUID.fromString(refreshToken.getSubjectId())))
            .orElseThrow(() -> new InvalidTokenException("Customer not found"));

        if (!customer.isActive() || !customer.isPortalAccess()) {
            throw new InvalidTokenException("Account is not active or portal access is disabled");
        }

        Authenticable authenticable = new CustomerAuthenticableAdapter(customer);

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String accessToken = tokenService.generateAccessToken(authenticable);
        RefreshToken newRefreshToken = tokenService.generateRefreshToken(authenticable);
        refreshTokenRepository.save(newRefreshToken);

        PortalCustomerResponse customerResponse = new PortalCustomerResponse(
            customer.getLegalName().getValue(),
            customer.getRif().getValue()
        );

        return PortalAuthResponse.of(
            accessToken,
            newRefreshToken.getToken(),
            tokenService.getAccessTokenExpiration(),
            customerResponse
        );
    }
}
