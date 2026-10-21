package com.cashly.cashly_api.platform.auth.token.application.refresh.use_case;

import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.cashly.cashly_api.platform.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.token.application.refresh.request.RefreshTokenRequest;
import com.cashly.cashly_api.platform.auth.token.domain.repository.RefreshTokenRepository;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.platform.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.platform.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.platform.auth.token.application.exceptions.InvalidTokenException;

import org.springframework.transaction.annotation.Transactional;

public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticableSubjectGateway subjectGateway;
    private final TokenService tokenService;
    private final com.cashly.cashly_api.platform.auth.mfa.application.MfaStore mfaStore;

    public RefreshTokenUseCase(
        RefreshTokenRepository refreshTokenRepository,
        AuthenticableSubjectGateway subjectGateway,
        TokenService tokenService,
        com.cashly.cashly_api.platform.auth.mfa.application.MfaStore mfaStore
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.subjectGateway = subjectGateway;
        this.tokenService = tokenService;
        this.mfaStore = mfaStore;
    }

    @Transactional
    public AuthenticationResponse execute(RefreshTokenRequest request) {
        String subjectId = refreshTokenRepository.findSubjectIdByToken(request.refreshToken())
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        mfaStore.lock(subjectId);

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
