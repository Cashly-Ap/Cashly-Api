package com.parsun.parsun_api.auth.authentication.infrastructure.security;

import com.parsun.parsun_api.auth.authentication.domain.entities.AuthenticableType;
import com.parsun.parsun_api.auth.token.domain.services.TokenService;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterUnitTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private UserId testUserId;
    private Customer testCustomer;
    private CustomerId testCustomerId;
    private String validToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenService, userRepository, customerRepository);

        testUserId = UserId.generate();
        Email email = new Email("test@example.com");
        Password password = Password.fromHash("$2a$10$hashedPassword");
        testUser = new User(testUserId, email, new UserName("Test User"), password, UserRole.ADMIN);

        testCustomerId = CustomerId.generate();
        Rif rif = new Rif("J-12345678-9");
        testCustomer = Customer.reconstitute(
            testCustomerId, new CustomerCode(1001L), new LegalName("Test Customer"),
            CustomerType.BUSINESS, rif, new CustomerAlias("TestAlias"),
            new CreditDays(30), new CreditLimit(BigDecimal.valueOf(1000)),
            true, LocalDateTime.now(), LocalDateTime.now(),
            List.of(), List.of(), List.of(),
            true, "$2a$10$hashedPassword", null, null
        );

        validToken = "valid-jwt-token";
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Test
        @DisplayName("should_AuthenticateUser_When_ValidTokenProvided")
        void should_AuthenticateUser_When_ValidTokenProvided() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.USER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testUserId.getValue().toString());
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(testUserId.getValue().toString(), SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_SetUserRole_When_UserAuthenticated")
        void should_SetUserRole_When_UserAuthenticated() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.USER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testUserId.getValue().toString());
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_UserNotFound")
        void should_NotAuthenticate_When_UserNotFound() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.USER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testUserId.getValue().toString());
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_UserInactive")
        void should_NotAuthenticate_When_UserInactive() throws Exception {
            testUser.deactivate();

            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.USER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testUserId.getValue().toString());
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Customer Authentication Tests")
    class CustomerAuthenticationTests {

        @Test
        @DisplayName("should_AuthenticateCustomer_When_ValidTokenProvided")
        void should_AuthenticateCustomer_When_ValidTokenProvided() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.CUSTOMER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testCustomerId.getValue().toString());
            when(customerRepository.isActivePortalAccess(testCustomerId)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(testCustomerId.getValue().toString(), SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_SetPortalCustomerRole_When_CustomerAuthenticated")
        void should_SetPortalCustomerRole_When_CustomerAuthenticated() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.CUSTOMER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testCustomerId.getValue().toString());
            when(customerRepository.isActivePortalAccess(testCustomerId)).thenReturn(true);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PORTAL_CUSTOMER")));
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_CustomerNotFound")
        void should_NotAuthenticate_When_CustomerNotFound() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.CUSTOMER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testCustomerId.getValue().toString());
            when(customerRepository.isActivePortalAccess(testCustomerId)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_CustomerInactive")
        void should_NotAuthenticate_When_CustomerInactive() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.CUSTOMER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testCustomerId.getValue().toString());
            when(customerRepository.isActivePortalAccess(testCustomerId)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_CustomerPortalAccessDisabled")
        void should_NotAuthenticate_When_CustomerPortalAccessDisabled() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(true);
            when(tokenService.extractSubjectType(validToken)).thenReturn(AuthenticableType.CUSTOMER);
            when(tokenService.extractSubjectId(validToken)).thenReturn(testCustomerId.getValue().toString());
            when(customerRepository.isActivePortalAccess(testCustomerId)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Common Authentication Tests")
    class CommonAuthenticationTests {

        @Test
        @DisplayName("should_NotAuthenticate_When_NoAuthorizationHeader")
        void should_NotAuthenticate_When_NoAuthorizationHeader() throws Exception {
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(tokenService, never()).validateToken(anyString());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_InvalidTokenFormat")
        void should_NotAuthenticate_When_InvalidTokenFormat() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("InvalidFormat " + validToken);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(tokenService, never()).validateToken(anyString());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_NotAuthenticate_When_TokenValidationFails")
        void should_NotAuthenticate_When_TokenValidationFails() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(tokenService, times(1)).validateToken(validToken);
            verify(userRepository, never()).findById(any());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_ContinueFilterChain_When_ExceptionOccurs")
        void should_ContinueFilterChain_When_ExceptionOccurs() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
            when(tokenService.validateToken(validToken)).thenThrow(new RuntimeException("Token validation error"));

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_HandleEmptyBearerToken_When_OnlyPrefixPresent")
        void should_HandleEmptyBearerToken_When_OnlyPrefixPresent() throws Exception {
            when(request.getHeader("Authorization")).thenReturn("Bearer ");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("should_CallFilterChain_When_ProcessingComplete")
        void should_CallFilterChain_When_ProcessingComplete() throws Exception {
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain, times(1)).doFilter(request, response);
        }
    }
}
