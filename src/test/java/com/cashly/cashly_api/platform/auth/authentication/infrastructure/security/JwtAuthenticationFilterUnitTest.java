package com.cashly.cashly_api.platform.auth.authentication.infrastructure.security;

import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterUnitTest {
    private static final String TOKEN = "valid-access-token";
    private static final UUID USER_ID = UUID
            .fromString("30000000-0000-0000-0000-000000000001");
    private static final UUID ROLE_ID = UUID
            .fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID DEPARTMENT_ID = UUID
            .fromString("20000000-0000-0000-0000-000000000001");

    @Mock
    private TokenService tokens;
    @Mock
    private UserRepository users;
    @Mock
    private AccessService access;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(tokens, users, access);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesSubjectWithCurrentPersistedGrants() throws Exception {
        validToken();
        when(access.resolve(USER_ID))
                .thenReturn(current(Set.of("CATALOG_PRODUCTS_VIEW_LIST")));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()
                .getName()).isEqualTo(USER_ID.toString());
        verify(access).resolve(USER_ID);
        verify(chain).doFilter(request, response);
    }

    @Test
    void emitsOnlyCurrentModuleActionAuthorities() throws Exception {
        validToken();
        when(access.resolve(USER_ID))
                .thenReturn(current(Set.of("CATALOG_PRODUCTS_VIEW_LIST")));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("CATALOG_PRODUCTS_VIEW_LIST");
    }

    @Test
    void doesNotRestoreRevokedGrantFromAnAlreadyIssuedToken() throws Exception {
        validToken();
        when(access.resolve(USER_ID))
                .thenReturn(current(
                        Set.of("CATALOG_PRODUCTS_VIEW_LIST", "CATALOG_PRODUCTS_EDIT")))
                .thenReturn(current(Set.of("CATALOG_PRODUCTS_VIEW_LIST")));
        filter.doFilterInternal(request, response, chain);
        SecurityContextHolder.clearContext();

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .doesNotContain("CATALOG_PRODUCTS_EDIT");
    }

    @Test
    void rejectsInactiveAccountOrRole() throws Exception {
        validToken();
        when(access.resolve(USER_ID))
                .thenThrow(new AccessDeniedException("Account unavailable"));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void failsClosedWhenAuthoritativeLookupFails() throws Exception {
        validToken();
        when(access.resolve(USER_ID))
                .thenThrow(new IllegalStateException("Database unavailable"));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void ignoresMissingAuthorizationHeader() throws Exception {
        filter.doFilterInternal(request, response, chain);

        verifyNoInteractions(tokens, access);
        verify(chain).doFilter(request, response);
    }

    @Test
    void ignoresOtherAuthorizationSchemes() throws Exception {
        when(request.getHeader("Authorization"))
                .thenReturn("Basic credentials");

        filter.doFilterInternal(request, response, chain);

        verifyNoInteractions(tokens, access);
    }

    @Test
    void rejectsInvalidTokenBeforeResolvingPermissions() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokens.validateToken(TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, chain);

        verifyNoInteractions(access);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void continuesFilterChainAfterTokenFailure() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokens.validateToken(TOKEN))
                .thenThrow(new IllegalArgumentException("Malformed token"));

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    private void validToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokens.validateToken(TOKEN)).thenReturn(true);
        when(tokens.extractSubjectType(TOKEN))
                .thenReturn(AuthenticableType.USER);
        when(tokens.extractSubjectId(TOKEN)).thenReturn(USER_ID.toString());
    }

    private AccessService.CurrentAccess current(Set<String> permissions) {
        return new AccessService.CurrentAccess(ROLE_ID, "Custom role",
                DEPARTMENT_ID, false, permissions);
    }
}
