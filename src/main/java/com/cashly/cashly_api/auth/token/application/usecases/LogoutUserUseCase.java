package com.cashly.cashly_api.auth.token.application.usecases;

import com.cashly.cashly_api.auth.token.application.dto.request.LogoutRequest;
import com.cashly.cashly_api.auth.token.domain.repositories.RefreshTokenRepository;
import com.cashly.cashly_api.auth.token.domain.entities.RefreshToken;
import com.cashly.cashly_api.auth.token.application.exceptions.InvalidTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUserUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUserUseCase(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void execute(LogoutRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }
}
