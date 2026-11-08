package com.cashly.cashly_api.platform.audit.infrastructure.web;

import com.cashly.cashly_api.platform.audit.application.dto.response.AuditActorResponse;
import com.cashly.cashly_api.platform.audit.application.dto.response.AuditDateRangeResponse;
import com.cashly.cashly_api.platform.audit.application.dto.response.AuditEventResponse;
import com.cashly.cashly_api.platform.audit.application.dto.response.AuditFilterOptionsResponse;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditQueryService;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.config.SecurityConfig;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationEntryPoint;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationFilter;
import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class AuditControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AuditQueryService queries;

    @MockitoBean
    JwtAuthenticationFilter jwtFilter;

    @MockitoBean
    UserPreferencesRepository preferences;

    @MockitoBean(name = "corsConfigurationSource")
    CorsConfigurationSource cors;

    private final UUID entityId = UUID.randomUUID();

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(invocation -> {
            ((jakarta.servlet.FilterChain) invocation.getArgument(2)).doFilter(
                    invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
        when(queries.find(any())).thenReturn(
                new PageResponse<>(List.of(), 0, 0, 20, 0, true, true, true));
        when(queries.filterOptions(any(), any(), any())).thenReturn(
                new AuditFilterOptionsResponse(List.of(), List.of(),
                        new AuditDateRangeResponse(null, null)));
        when(queries.get(any())).thenReturn(new AuditEventResponse(
                UUID.randomUUID(), "finance", "INVOICE", entityId, "INV-1",
                null, "CONFIRM", new AuditActorResponse(null, "Sistema", null),
                Instant.parse("2025-01-01T10:00:00Z"),
                "audit.events.confirm.description", List.of("INV-1"),
                Map.of()));
    }

    @Test
    void returnsPagedHistoryForARecord() throws Exception {
        mvc.perform(get("/api/audit-events").param("entityType", "INVOICE")
                .param("entityId", entityId.toString())
                .with(user("reader").authorities(
                        new SimpleGrantedAuthority("AUDIT_TRAIL_VIEW_LIST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.empty").value(true));
        verify(queries).find(any());
    }

    @Test
    void returnsOnlyValuesForTheRecord() throws Exception {
        mvc.perform(get("/api/audit-events/filter-options")
                .param("entityType", "INVOICE")
                .param("entityId", entityId.toString())
                .with(user("reader").authorities(
                        new SimpleGrantedAuthority("AUDIT_TRAIL_VIEW_LIST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actions.length()").value(0));
    }

    @Test
    void returnsEventDetail() throws Exception {
        mvc.perform(get("/api/audit-events/" + UUID.randomUUID())
                .with(user("reader").authorities(
                        new SimpleGrantedAuthority("AUDIT_TRAIL_VIEW_LIST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("CONFIRM"));
    }

    @Test
    void rejectsMissingEntityScope() throws Exception {
        mvc.perform(get("/api/audit-events").with(user("reader").authorities(
                new SimpleGrantedAuthority("AUDIT_TRAIL_VIEW_LIST"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUDIT_INVALID_FILTER"));
    }

    @Test
    void rejectsUnknownEntityType() throws Exception {
        mvc.perform(get("/api/audit-events").param("entityType", "UNKNOWN")
                .param("entityId", entityId.toString())
                .with(user("reader").authorities(
                        new SimpleGrantedAuthority("AUDIT_TRAIL_VIEW_LIST"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUDIT_INVALID_FILTER"));
    }

    @Test
    void rejectsMissingPermission() throws Exception {
        mvc.perform(get("/api/audit-events").param("entityType", "INVOICE")
                .param("entityId", entityId.toString())
                .with(user("writer").authorities(
                        new SimpleGrantedAuthority("INVOICES_VIEW_LIST"))))
                .andExpect(status().isForbidden());
    }
}
