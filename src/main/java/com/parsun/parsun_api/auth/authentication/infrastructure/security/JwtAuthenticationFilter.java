package com.parsun.parsun_api.auth.authentication.infrastructure.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;

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

    public JwtAuthenticationFilter(TokenService tokenService,
                                   UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

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

        switch (subjectType) {
            case USER -> authenticateUser(request, subjectId);
        }
    }

    private void authenticateUser(HttpServletRequest request, String subjectId) {
        UserId userId = new UserId(UUID.fromString(subjectId));

        userRepository.findById(userId)
            .filter(User::canAuthenticate)
            .ifPresent(user -> setUserAuthentication(request, user));
    }

    private void setUserAuthentication(HttpServletRequest request, User user) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            user.getId().getValue().toString(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
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
