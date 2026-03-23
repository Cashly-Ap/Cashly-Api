package com.parsun.parsun_api.auth.token.domain.services;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;

import java.time.LocalDateTime;

public interface TokenService {

    String generateAccessToken(User user);

    String generateAccessToken(Authenticable authenticable);

    RefreshToken generateRefreshToken(User user);

    RefreshToken generateRefreshToken(Authenticable authenticable);

    boolean validateToken(String token);

    UserId extractUserId(String token);

    String extractSubjectId(String token);

    AuthenticableType extractSubjectType(String token);

    Email extractEmail(String token);

    LocalDateTime getExpirationDate(String token);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();
}
