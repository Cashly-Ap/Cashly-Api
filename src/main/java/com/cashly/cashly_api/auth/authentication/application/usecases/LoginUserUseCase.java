package com.cashly.cashly_api.auth.authentication.application.usecases;

import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticatedEntityResponse;
import com.cashly.cashly_api.auth.authentication.application.dto.response.AuthenticationResponse;
import com.cashly.cashly_api.auth.authentication.application.dto.request.LoginRequest;
import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.services.AuthenticableResolver;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.token.domain.entities.RefreshToken;
import com.cashly.cashly_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import org.springframework.transaction.annotation.Transactional;

public class LoginUserUseCase {

    private final AuthenticableResolver authenticableResolver;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public LoginUserUseCase(
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
    public AuthenticationResponse execute(LoginRequest request) {
        String email = normalizeEmail(request.email());

        Authenticable authenticable = authenticableResolver.resolveByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!authenticable.canAuthenticate()) {
            throw new InvalidCredentialsException("Account is not active");
        }

        if (!passwordEncoder.matches(request.password(), authenticable.getHashedPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = tokenService.generateAccessToken(authenticable);

        RefreshToken refreshToken = tokenService.generateRefreshToken(authenticable);
        refreshTokenRepository.save(refreshToken);

        AuthenticatedEntityResponse entityResponse = AuthenticatedEntityResponse.from(authenticable);
        return AuthenticationResponse.of(
            accessToken,
            refreshToken.getToken(),
            tokenService.getAccessTokenExpiration(),
            entityResponse
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
