package com.parsun.parsun_api.auth.authentication.infrastructure.security;

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

import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;

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
    private final CustomerRepository customerRepository;

    public JwtAuthenticationFilter(TokenService tokenService,
                                   UserRepository userRepository,
                                   CustomerRepository customerRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
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
            case CUSTOMER -> authenticateCustomer(request, subjectId);
        }
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

    private void authenticateCustomer(HttpServletRequest request, String subjectId) {
        CustomerId customerId = CustomerId.from(subjectId);

        if (customerRepository.isActivePortalAccess(customerId)) {
            setCustomerAuthentication(request, customerId);
        }
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

    private void setCustomerAuthentication(HttpServletRequest request, CustomerId customerId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PORTAL_CUSTOMER"));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            customerId.getValue().toString(),
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
