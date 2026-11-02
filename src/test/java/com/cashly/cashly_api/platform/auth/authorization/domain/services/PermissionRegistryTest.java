package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PermissionRegistryTest {
    @ParameterizedTest
    @EnumSource(BusinessModule.class)
    void every_module_accepts_each_supported_action_independently(
            BusinessModule module) {
        var supported = PermissionRegistry.modules().get(module);
        var grant = supported.iterator().next();
        assertThat(PermissionRegistry.validate(Map.of()))
                .doesNotContainKey(module);
        assertThat(PermissionRegistry
                .validate(Map.of(module, Set.of(grant))))
                .containsEntry(module, Set.of(grant));
        if (module == BusinessModule.FINANCE_RECEIVABLES) {
            assertThatThrownBy(() -> PermissionRegistry.validate(Map.of(module, supported)))
                    .isInstanceOf(IllegalArgumentException.class);
        } else {
            assertThat(PermissionRegistry.validate(Map.of(module, supported)))
                    .containsEntry(module, supported);
        }
        for (var action : PermissionAction.values()) {
            if (!supported.contains(action)) {
                assertThatThrownBy(() -> PermissionRegistry
                        .validate(Map.of(module, Set.of(action))))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Test
    void unsupported_actions_and_custom_administrator_names_never_grant_access() {
        assertThatThrownBy(() -> PermissionRegistry
                .validate(Map.of(BusinessModule.DASHBOARD,
                        Set.of(PermissionAction.READ,
                                PermissionAction.CREATE))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                () -> PermissionRegistry.validate(Map.of(BusinessModule.CATALOG_PRODUCTS,
                        Set.of(PermissionAction.READ,
                                PermissionAction.IMPORT))))
                .isInstanceOf(IllegalArgumentException.class);
        var custom = new Role(UUID.randomUUID(), "SUPER_ADMIN", null, true, 0,
                null, Map.of());
        assertThat(custom.systemAdministrator()).isFalse();
        assertThat(custom.effectivePermissions()).isEmpty();
    }

    @Test
    void protected_system_key_gets_server_defined_grants() {
        var system = new Role(UUID.randomUUID(), "Any display name", null, true,
                0, "SYSTEM_ADMIN", Map.of());
        assertThat(system.effectivePermissions())
                .isEqualTo(PermissionRegistry.modules());
    }
}
