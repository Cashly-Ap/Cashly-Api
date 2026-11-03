package com.cashly.cashly_api.platform.auth.token.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtTokenServiceUnitTest {

    private static final String SECRET = "testSecretKeyThatIsAtLeast256BitsLongForHS256";

    private final JwtTokenService tokenService = new JwtTokenService(SECRET,
            900_000, 604_800_000);

    @Test
    void shouldEmbedRoleWithoutLegacyModuleClaims() {
        Authenticable authenticable = mock(Authenticable.class);
        when(authenticable.getSubjectId())
                .thenReturn("f8ccf44b-7508-4757-a907-7d7c149b8f71");
        when(authenticable.getEmail()).thenReturn("admin@cashly.test");
        when(authenticable.getType()).thenReturn(AuthenticableType.USER);
        when(authenticable.getRole()).thenReturn(UserRole.ADMIN);

        String token = tokenService.generateAccessToken(authenticable);

        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        SECRET.getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(token).getPayload();
        assertEquals("ADMIN", claims.get("role", String.class));
        assertNull(claims.get("modules"));
    }

}
