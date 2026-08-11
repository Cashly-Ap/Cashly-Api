package com.parsun.parsun_api.auth.token.domain.services;

import com.parsun.parsun_api.auth.authentication.domain.entities.Authenticable;
import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.entities.RefreshToken;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;

public interface TokenService {

    record UserAccess(UserRole role, Set<BusinessModule> modulePermissions) {
        public UserAccess {
            modulePermissions = Set.copyOf(modulePermissions);
        }
    }

    String generateAccessToken(User user);

    String generateAccessToken(Authenticable authenticable);

    RefreshToken generateRefreshToken(User user);

    RefreshToken generateRefreshToken(Authenticable authenticable);

    boolean validateToken(String token);

    UserId extractUserId(String token);

    String extractSubjectId(String token);

    AuthenticableType extractSubjectType(String token);

    Optional<UserAccess> extractUserAccess(String token);

    Email extractEmail(String token);

    LocalDateTime getExpirationDate(String token);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();
}
