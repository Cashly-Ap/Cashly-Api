package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import com.cashly.cashly_api.platform.auth.authentication.infrastructure.config.SecurityConfig;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationEntryPoint;
import com.cashly.cashly_api.platform.auth.authentication.infrastructure.security.JwtAuthenticationFilter;
import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionCatalogController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class PermissionCatalogControllerTest {

    private static final String CATALOG = "/api/permissions/catalog";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JwtAuthenticationFilter jwtFilter;

    @MockitoBean
    UserPreferencesRepository preferences;

    @MockitoBean(name = "corsConfigurationSource")
    CorsConfigurationSource cors;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(invocation -> {
            ((jakarta.servlet.FilterChain) invocation.getArgument(2)).doFilter(
                    invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(),
                any(),
                any());
    }

    @Test
    void readerReceivesTheAreasSubmodulesAndDomainsInDeterministicOrder()
            throws Exception {
        mvc.perform(get(CATALOG).with(user("reader")
                .authorities(new SimpleGrantedAuthority("ROLES_VIEW_LIST"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].area").value("SALES"))
                .andExpect(jsonPath("$[0].sortOrder").value(10))
                .andExpect(jsonPath("$[0].labelKey")
                        .value("platform.auth.areas.sales"))
                .andExpect(jsonPath("$[0].submodules[0].domains[0].module")
                        .value("SALES_QUOTES"))
                .andExpect(jsonPath(
                        "$[0].submodules[0].domains[0].permissions[0].code")
                        .value("SALES_QUOTES_VIEW_LIST"))
                .andExpect(jsonPath(
                        "$[0].submodules[0].domains[0].permissions[0].labelKey")
                        .value("platform.auth.permissions.sales_quotes.view_list"))
                .andExpect(jsonPath(
                        "$[0].submodules[0].domains[0].permissions[0].descriptionKey")
                        .value("platform.auth.permissions.sales_quotes.view_list.description"))
                .andExpect(jsonPath("$[1].area").value("PURCHASES"))
                .andExpect(jsonPath("$[2].area").value("CATALOG"))
                .andExpect(jsonPath("$[3].area").value("INVENTORY"))
                .andExpect(jsonPath("$[4].area").value("FINANCE"))
                .andExpect(jsonPath("$[4].submodules[0].submodule")
                        .value("FINANCE_BILLING"))
                .andExpect(jsonPath("$[4].submodules[1].submodule")
                        .value("FINANCE_TREASURY"))
                .andExpect(jsonPath("$[4].submodules[1].domains[1].module")
                        .value("FINANCE_TREASURY_RECEIVABLES"))
                .andExpect(jsonPath("$[5].area").value("CONTACTS"))
                .andExpect(jsonPath("$[6].area").value("ANALYTICS"))
                .andExpect(jsonPath("$[7].area").value("PLATFORM"))
                .andExpect(jsonPath("$[7].submodules[0].domains.length()")
                        .value(5))
                .andExpect(jsonPath("$[7].submodules[0].domains[3].module")
                        .value("COMPANY"))
                .andExpect(jsonPath("$[7].submodules[0].domains[4].module")
                        .value("AUDIT_TRAIL"));
    }

    @Test
    void catalogExposesCompanyUnderPlatform() throws Exception {
        var body = mvc.perform(get(CATALOG).with(user("reader")
                .authorities(new SimpleGrantedAuthority("ROLES_VIEW_LIST"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(body)
                .contains("\"COMPANY\"", "COMPANY_VIEW", "COMPANY_EDIT")
                .doesNotContain("\"DOCUMENTS\"");
    }

    @Test
    void readerWithoutTheCatalogAuthorityIsForbidden() throws Exception {
        mvc.perform(get(CATALOG).with(user("writer")
                .authorities(new SimpleGrantedAuthority("ROLES_CREATE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserWithAnUnrelatedDomainIsForbidden() throws Exception {
        mvc.perform(get(CATALOG).with(user("sales")
                .authorities(new SimpleGrantedAuthority("SALES_ORDERS_VIEW_LIST"),
                        new SimpleGrantedAuthority(
                                "SALES_ORDERS_CONFIRM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousRequestIsUnauthorized() throws Exception {
        mvc.perform(get(CATALOG)).andExpect(status().isUnauthorized());
    }
}
