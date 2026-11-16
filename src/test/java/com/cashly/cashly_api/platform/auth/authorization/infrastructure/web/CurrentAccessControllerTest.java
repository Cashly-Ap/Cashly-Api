package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrentAccessControllerTest {

    private final AccessService access = mock(AccessService.class);

    @Test
    void returnsFlatPermissionsAndPreventsCaching() throws Exception {
        var roleId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        when(access.current()).thenReturn(new AccessService.CurrentAccess(roleId,
                "Operador de ventas", departmentId, false,
                Set.of("SALES_QUOTES_VIEW_LIST", "SALES_QUOTES_CREATE")));

        MockMvcBuilders.standaloneSetup(new CurrentAccessController(access))
                .build()
                .perform(get("/api/auth/access"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.roleId").value(roleId.toString()))
                .andExpect(jsonPath("$.roleName").value("Operador de ventas"))
                .andExpect(jsonPath("$.departmentId")
                        .value(departmentId.toString()))
                .andExpect(jsonPath("$.systemAdministrator").value(false))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions.length()").value(2));
    }

    @Test
    void returnsAnEmptyFlatArrayForAnAccountWithoutPermissions()
            throws Exception {
        when(access.current()).thenReturn(new AccessService.CurrentAccess(
                UUID.randomUUID(), "Sin permisos", UUID.randomUUID(), false,
                Set.of()));

        MockMvcBuilders.standaloneSetup(new CurrentAccessController(access))
                .build()
                .perform(get("/api/auth/access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions.length()").value(0))
                .andExpect(jsonPath("$.systemAdministrator").value(false));
    }
}
