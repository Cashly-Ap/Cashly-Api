package com.cashly.cashly_api.platform.auth.authentication.infrastructure.web;

import com.cashly.cashly_api.platform.auth.authentication.application.login.use_case.LoginUserUseCase;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.config.SecurityConfig;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationEntryPoint;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationFilter;
import com.cashly.cashly_api.platform.auth.token.application.logout.use_case.LogoutUserUseCase;
import com.cashly.cashly_api.platform.auth.token.application.refresh.use_case.RefreshTokenUseCase;
import com.cashly.cashly_api.platform.auth.user.application.password.use_case.UpdateUserPasswordUseCase;
import com.cashly.cashly_api.platform.auth.user.application.queries.use_case.GetUserByIdUseCase;
import com.cashly.cashly_api.platform.auth.user.application.register.use_case.RegisterUserUseCase;
import com.cashly.cashly_api.platform.auth.user.application.dto.response.UserResponse;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class OwnProfileControllerTest {
    @Autowired MockMvc mvc;
    @Autowired org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mappings;
    @MockitoBean RegisterUserUseCase register;
    @MockitoBean LoginUserUseCase login;
    @MockitoBean RefreshTokenUseCase refresh;
    @MockitoBean LogoutUserUseCase logout;
    @MockitoBean UpdateUserPasswordUseCase password;
    @MockitoBean GetUserByIdUseCase query;
    @MockitoBean com.cashly.cashly_api.platform.auth.user.application.update.use_case.UpdateOwnProfileUseCase update;
    @MockitoBean JwtAuthenticationFilter jwtFilter;
    @MockitoBean(name = "corsConfigurationSource") CorsConfigurationSource cors;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void ownProfileUsesAuthenticatedIdentityWithoutUserReadPermissionOrCredentials() throws Exception {
        when(query.execute("own-user")).thenReturn(new UserResponse("own-user", "own@example.com",
                "Own User", UserRole.OPERATOR, true, true, false, Set.of(), "Own", "User",
                null, "+34600000000", null, null, "Sales", "Operations", null, null));

        mvc.perform(get("/api/auth/profile").with(user("own-user").authorities())
                        .param("id", "other-user").param("userId", "other-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("own-user"))
                .andExpect(jsonPath("$.phone").value("+34600000000"))
                .andExpect(jsonPath("$.roleName").value("Sales"))
                .andExpect(jsonPath("$.departmentName").value("Operations"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
        verify(query).execute("own-user");
        verifyNoMoreInteractions(query);
    }

    @Test
    void anonymousProfileAccessIsDeniedBeforeReadingUser() throws Exception {
        mvc.perform(get("/api/auth/profile")).andExpect(status().isUnauthorized());
        verifyNoInteractions(query);
    }

    @Test
    void ownProfileUpdateDoesNotRequireAdministrativeAuthorities() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/auth/profile")
                        .with(user("own-user").authorities()).param("id", "other-user")
                        .contentType("application/json")
                        .content("{\"userName\":\"New Name\",\"firstName\":\"First\",\"lastName\":\"Last\",\"phone\":\"+34600000000\",\"address\":null}"))
                .andExpect(status().isOk());
        verify(update).execute(eq("own-user"), any());
        verifyNoMoreInteractions(update);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"roleId", "departmentId", "permissions", "modulePermissions",
            "role", "systemKey", "active", "emailVerified", "email", "password", "id"})
    void protectedFieldsAreRejectedEvenWhenNull(String field) throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/auth/profile")
                        .with(user("own-user").authorities()).contentType("application/json")
                        .content("{\"userName\":\"New Name\",\"firstName\":\"First\",\"lastName\":\"Last\",\"" + field + "\":null}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(update);
    }

    @Test
    void anonymousProfileUpdateIsDenied() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/auth/profile")
                        .contentType("application/json")
                        .content("{\"userName\":\"New Name\",\"firstName\":\"First\",\"lastName\":\"Last\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(update);
    }

    @Test
    void profileHasNoRouteToSelectAnotherUser() throws Exception {
        var profileRoutes = mappings.getHandlerMethods().entrySet().stream()
                .filter(entry -> entry.getValue().getBeanType().equals(AuthController.class)
                        && entry.getValue().getMethod().getName().equals("profile"))
                .flatMap(entry -> entry.getKey().getPatternValues().stream()).toList();
        org.assertj.core.api.Assertions.assertThat(profileRoutes).containsExactly("/api/auth/profile");
        verifyNoInteractions(query);
    }
}
