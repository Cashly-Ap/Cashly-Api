package com.cashly.cashly_api.platform.auth.authentication.infrastructure.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService accessService;
    public JwtAuthenticationFilter(TokenService tokenService,
            UserRepository userRepository,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService accessService) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.accessService = accessService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && tokenService.validateToken(jwt)) {
                authenticateSubject(request, jwt);
            }
        } catch (Exception e) {
            logger.error("Authentication error: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateSubject(HttpServletRequest request, String jwt) {
        AuthenticableType subjectType = tokenService.extractSubjectType(jwt);
        String subjectId = tokenService.extractSubjectId(jwt);

        authenticateUser(request, subjectId);
    }

    private void authenticateUser(HttpServletRequest request,
            String subjectId) {
        var access = accessService.resolve(UUID.fromString(subjectId));
        List<GrantedAuthority> authorities = new ArrayList<>();
        access.permissions().forEach(
                code -> authorities.add(new SimpleGrantedAuthority(code)));
        var authentication = new UsernamePasswordAuthenticationToken(subjectId,
                null, authorities);
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
