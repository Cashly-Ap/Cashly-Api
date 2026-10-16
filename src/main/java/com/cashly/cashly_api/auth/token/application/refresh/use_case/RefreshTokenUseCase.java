package com.cashly.cashly_api.auth.token.application.refresh.use_case;

import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.auth.token.application.refresh.request.RefreshTokenRequest;
import com.cashly.cashly_api.auth.token.domain.repository.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.auth.token.application.exceptions.InvalidTokenException;

import org.springframework.transaction.annotation.Transactional;

public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticableSubjectGateway subjectGateway;
    private final TokenService tokenService;

    public RefreshTokenUseCase(
        RefreshTokenRepository refreshTokenRepository,
        AuthenticableSubjectGateway subjectGateway,
        TokenService tokenService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.subjectGateway = subjectGateway;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthenticationResponse execute(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        Authenticable authenticable = subjectGateway.resolve(
            refreshToken.getSubjectId(),
            refreshToken.getSubjectType()
        );

        if (!authenticable.canAuthenticate()) {
            throw new InvalidTokenException("Account is not active");
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String accessToken = tokenService.generateAccessToken(authenticable);
        RefreshToken newRefreshToken = tokenService.generateRefreshToken(authenticable);
        refreshTokenRepository.save(newRefreshToken);

        AuthenticatedEntityResponse entityResponse = AuthenticatedEntityResponse.from(authenticable);
        return AuthenticationResponse.of(
            accessToken,
            newRefreshToken.getToken(),
            tokenService.getAccessTokenExpiration(),
            entityResponse
        );
    }

}
