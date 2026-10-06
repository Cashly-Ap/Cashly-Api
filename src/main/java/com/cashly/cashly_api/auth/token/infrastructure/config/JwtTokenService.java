package com.cashly.cashly_api.auth.token.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cashly.cashly_api.auth.authentication.domain.entities.Authenticable;
import com.cashly.cashly_api.auth.authentication.domain.entities.AuthenticableType;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.token.domain.entities.RefreshToken;
import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService implements TokenService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenService(
        @Value("${jwt.secret:defaultSecretKeyThatIsAtLeast256BitsLongForHS256Algorithm}") String secret,
        @Value("${jwt.access-token-expiration:900000}") long accessTokenExpiration,
        @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    public String generateAccessToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
            .subject(user.getId().getValue().toString())
            .claim("email", user.getEmail().getValue())
            .claim("type", AuthenticableType.USER.name())
            .claim("role", user.getRole().name())
            .claim("modules", user.getModulePermissions().stream().map(Enum::name).toList())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    @Override
    public String generateAccessToken(Authenticable authenticable) {
        if (authenticable == null) {
            throw new IllegalArgumentException("Authenticable cannot be null");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        var builder = Jwts.builder()
            .subject(authenticable.getSubjectId())
            .claim("email", authenticable.getEmail())
            .claim("type", authenticable.getType().name())
            .issuedAt(now);

        if (authenticable.getType() == AuthenticableType.USER) {
            builder.claim("role", authenticable.getRole().name())
                .claim("modules", authenticable.getModulePermissions().stream().map(Enum::name).toList());
        }

        return builder
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact();
    }

    @Override
    public RefreshToken generateRefreshToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        return new RefreshToken(
            RefreshTokenId.generate(),
            user.getId().getValue().toString(),
            AuthenticableType.USER,
            token,
            expiresAt
        );
    }

    @Override
    public RefreshToken generateRefreshToken(Authenticable authenticable) {
        if (authenticable == null) {
            throw new IllegalArgumentException("Authenticable cannot be null");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        return new RefreshToken(
            RefreshTokenId.generate(),
            authenticable.getSubjectId(),
            authenticable.getType(),
            token,
            expiresAt
        );
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException | io.jsonwebtoken.ExpiredJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public UserId extractUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Claims claims = extractClaims(token);
        String userIdStr = claims.getSubject();
        return new UserId(UUID.fromString(userIdStr));
    }

    @Override
    public String extractSubjectId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Claims claims = extractClaims(token);
        return claims.getSubject();
    }

    @Override
    public AuthenticableType extractSubjectType(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Claims claims = extractClaims(token);
        String type = claims.get("type", String.class);
        if (type == null) {
            return AuthenticableType.USER;
        }
        return AuthenticableType.valueOf(type);
    }

    @Override
    public Optional<UserAccess> extractUserAccess(String token) {
        Claims claims = extractClaims(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            return Optional.empty();
        }

        List<?> modules = claims.get("modules", List.class);
        Set<BusinessModule> modulePermissions = modules == null ? Set.of() : modules.stream()
            .map(String::valueOf)
            .map(BusinessModule::valueOf)
            .collect(Collectors.toUnmodifiableSet());

        return Optional.of(new UserAccess(UserRole.valueOf(role), modulePermissions));
    }

    @Override
    public Email extractEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Claims claims = extractClaims(token);
        String email = claims.get("email", String.class);
        return new Email(email);
    }

    @Override
    public LocalDateTime getExpirationDate(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        Claims claims = extractClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    @Override
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    @Override
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
