package com.cashly.cashly_api.platform.auth.preferences;

import com.cashly.cashly_api.platform.auth.preferences.infrastructure.web.UserPreferencesController;
import com.cashly.cashly_api.platform.auth.preferences.application.UserPreferencesService;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;
import com.cashly.cashly_api.platform.auth.authorization.infrastructure.security.SecurityContextCurrentActor;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.config.SecurityConfig;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationEntryPoint;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.Optional;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserPreferencesController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, UserPreferencesService.class, SecurityContextCurrentActor.class})
class UserPreferencesControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean UserPreferencesRepository repository;
    @MockitoBean JwtAuthenticationFilter jwtFilter;
    @MockitoBean(name="corsConfigurationSource") CorsConfigurationSource cors;
    final UUID own = UUID.randomUUID();
    @BeforeEach void setup() throws Exception {
        doAnswer(invocation -> { ((jakarta.servlet.FilterChain) invocation.getArgument(2)).doFilter(invocation.getArgument(0), invocation.getArgument(1)); return null; })
            .when(jwtFilter).doFilter(any(), any(), any());
        when(repository.findByUserId(own)).thenReturn(Optional.empty());
    }
    @Test void authenticatedUserWithoutAdministrativePermissionsReadsOwnDefaults() throws Exception {
        mvc.perform(get("/api/auth/preferences").with(user(own.toString()).authorities()).param("userId", UUID.randomUUID().toString()))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(content().json("{\"themeMode\":\"system\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":false,\"showShortcutHints\":true}"));
        verify(repository).findByUserId(own);
    }
    @Test void administratorAlsoUpdatesOnlyOwnAccount() throws Exception {
        mvc.perform(put("/api/auth/preferences").with(user(own.toString()).roles("SUPER_ADMIN"))
            .param("userId", UUID.randomUUID().toString()).contentType("application/json")
            .content("{\"themeMode\":\"light\",\"colorMode\":\"informative\",\"shortcutsEnabled\":true,\"showShortcutHints\":false}"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
        verify(repository).save(eq(own), any());
    }
    @Test void hiddenHintsAreReturnedAndPersistedForOwnAccount() throws Exception {
        mvc.perform(put("/api/auth/preferences").with(user(own.toString()).authorities())
            .contentType("application/json")
            .content("{\"themeMode\":\"system\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":true,\"showShortcutHints\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.showShortcutHints").value(false));
        verify(repository).save(eq(own), argThat(p -> !p.showShortcutHints()));
    }
    @ParameterizedTest
    @ValueSource(strings={"{\"themeMode\":\"system\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":true}", "{\"themeMode\":\"system\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":true,\"showShortcutHints\":null}"})
    void rejectsMissingOrNullHintPreference(String body) throws Exception {
        mvc.perform(put("/api/auth/preferences").with(user(own.toString())).contentType("application/json").content(body))
            .andExpect(status().isBadRequest());
        verify(repository, never()).save(any(), any());
    }
    @Test void persistsEnglishAndRejectsUnsupportedLanguage() throws Exception {
        String body = "{\"themeMode\":\"system\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":false,\"showShortcutHints\":true,\"language\":\"en\"}";
        mvc.perform(put("/api/auth/preferences").with(user(own.toString())).contentType("application/json").content(body))
            .andExpect(status().isOk()).andExpect(jsonPath("$.language").value("en"));
        verify(repository).save(eq(own), argThat(p -> p.language().equals("en")));
        clearInvocations(repository);
        mvc.perform(put("/api/auth/preferences").with(user(own.toString())).contentType("application/json").content(body.replace("\"en\"", "\"fr\"")))
            .andExpect(status().isBadRequest());
        verify(repository, never()).save(any(), any());
    }
    @Test void anonymousRequestsCannotReadOrWrite() throws Exception {
        mvc.perform(get("/api/auth/preferences")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/auth/preferences").contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }
    @ParameterizedTest
    @ValueSource(strings={"{}", "{\"themeMode\":\"auto\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":false,\"showShortcutHints\":true}", "{\"themeMode\":\"light\",\"colorMode\":\"bad\",\"shortcutsEnabled\":false,\"showShortcutHints\":true}", "{\"themeMode\":\"light\",\"colorMode\":\"minimal\",\"shortcutsEnabled\":null}"})
    void rejectsInvalidOrMissingPreferences(String body) throws Exception {
        mvc.perform(put("/api/auth/preferences").with(user(own.toString())).contentType("application/json").content(body))
            .andExpect(status().isBadRequest());
        verify(repository, never()).save(any(), any());
    }
}
