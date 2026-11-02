package com.cashly.cashly_api.platform.auth.token.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

class JwtTokenServiceUnitTest {

    private final JwtTokenService tokenService = new JwtTokenService(
            "testSecretKeyThatIsAtLeast256BitsLongForHS256", 900_000,
            604_800_000);

    @Test
    void shouldEmbedUserAccessInSignedToken() {
        Authenticable authenticable = mock(Authenticable.class);
        when(authenticable.getSubjectId())
                .thenReturn("f8ccf44b-7508-4757-a907-7d7c149b8f71");
        when(authenticable.getEmail()).thenReturn("admin@cashly.test");
        when(authenticable.getType()).thenReturn(AuthenticableType.USER);
        when(authenticable.getRole()).thenReturn(UserRole.ADMIN);
        when(authenticable.getModulePermissions())
                .thenReturn(Set.of(BusinessModule.INVENTORY_WAREHOUSES));

        String token = tokenService.generateAccessToken(authenticable);

        var access = tokenService.extractUserAccess(token);
        assertTrue(access.isPresent());
        assertEquals(UserRole.ADMIN, access.orElseThrow().role());
        assertEquals(Set.of(BusinessModule.INVENTORY_WAREHOUSES),
                access.orElseThrow().modulePermissions());
    }

}
