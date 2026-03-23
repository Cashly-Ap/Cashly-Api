package com.parsun.parsun_api.auth.token.application.usecases;

import com.parsun.parsun_api.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.parsun.parsun_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.authentication.infrastructure.adapters.UserAuthenticableAdapter;
import com.parsun.parsun_api.auth.token.application.dto.request.RefreshTokenRequest;
import com.parsun.parsun_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.token.application.exceptions.InvalidTokenException;
import com.parsun.parsun_api.auth.user.application.exceptions.UserNotFoundException;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import org.springframework.transaction.annotation.Transactional;

public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public RefreshTokenUseCase(
        RefreshTokenRepository refreshTokenRepository,
        UserRepository userRepository,
        TokenService tokenService
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthenticationResponse execute(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        Authenticable authenticable = resolveAuthenticable(
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

    private Authenticable resolveAuthenticable(String subjectId, AuthenticableType subjectType) {
        return switch (subjectType) {
            case USER -> {
                User user = userRepository.findById(UserId.from(subjectId))
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
                yield new UserAuthenticableAdapter(user);
            }
        };
    }
}
