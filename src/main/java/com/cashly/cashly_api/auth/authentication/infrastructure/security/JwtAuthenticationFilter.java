package com.cashly.cashly_api.auth.authentication.infrastructure.security;

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

import com.cashly.cashly_api.auth.authentication.domain.AuthenticableType;
import com.cashly.cashly_api.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;

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

        authenticateUser(request, subjectId);
    }

    private void authenticateUser(HttpServletRequest request, String subjectId) {
        UserId userId = new UserId(UUID.fromString(subjectId));

        tokenService.extractUserAccess(extractJwtFromRequest(request))
            .ifPresentOrElse(
                access -> authenticateUserFromToken(request, userId, access),
                () -> authenticateUserFromRepository(request, userId)
            );
    }

    private void authenticateUserFromToken(HttpServletRequest request, UserId userId, TokenService.UserAccess access) {
        if (userRepository.canAuthenticate(userId)) {
            setUserAuthentication(request, userId.getValue().toString(), access.role(), access.modulePermissions());
        }
    }

    private void authenticateUserFromRepository(HttpServletRequest request, UserId userId) {
        userRepository.findById(userId)
            .filter(User::canAuthenticate)
            .ifPresent(user -> setUserAuthentication(
                request,
                user.getId().getValue().toString(),
                user.getRole(),
                user.getModulePermissions()
            ));
    }

    private void setUserAuthentication(HttpServletRequest request, String userId, UserRole role,
                                       java.util.Set<BusinessModule> modulePermissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        if (role == UserRole.SUPER_ADMIN) {
            for (BusinessModule module : BusinessModule.values()) {
                authorities.add(new SimpleGrantedAuthority("MODULE_" + module.name()));
            }
        } else {
            for (BusinessModule module : modulePermissions) {
                authorities.add(new SimpleGrantedAuthority("MODULE_" + module.name()));
            }
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userId,
            null,
            authorities
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
