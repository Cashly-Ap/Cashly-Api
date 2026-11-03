package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.authorization.domain.exceptions.InvalidPermissionDependencyException;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PermissionRegistryTest {

    @ParameterizedTest
    @EnumSource(PermissionModule.class)
    void every_module_accepts_each_supported_action_independently(
            PermissionModule module) {
        var supported = PermissionRegistry.modules().get(module);
        assertThat(supported).as(module.name()).isNotEmpty();
        var grant = supported.iterator().next();
        assertThat(PermissionRegistry.validate(Map.of()))
                .doesNotContainKey(module);
        assertThat(PermissionRegistry.validate(Map.of(module, Set.of(grant))))
                .containsEntry(module, Set.of(grant));
        assertThat(PermissionRegistry.validate(Map.of(module, supported)))
                .containsEntry(module, supported);
        for (var action : PermissionAction.values()) {
            if (!supported.contains(action)) {
                assertThatThrownBy(() -> PermissionRegistry
                        .validate(Map.of(module, Set.of(action))))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Test
    void declares_the_agreed_one_hundred_thirty_one_permissions() {
        assertThat(PermissionRegistry.codes()).hasSize(131).doesNotHaveDuplicates();
        assertThat(PermissionRegistry.modules()).hasSize(21);
    }

    @Test
    void unsupported_actions_and_custom_administrator_names_never_grant_access() {
        assertThatThrownBy(() -> PermissionRegistry
                .validate(Map.of(PermissionModule.DASHBOARD,
                        Set.of(PermissionAction.VIEW,
                                PermissionAction.CREATE))))
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
        assertThat(system.codes())
                .contains("COMPANY_EDIT",
                        "FINANCE_TREASURY_RECEIVABLES_PAY_DIRECT",
                        "CATALOG_PRODUCTS_DELETE");
    }

    @Test
    void rejects_child_permissions_without_the_module_view_list_root() {
        assertThatThrownBy(() -> PermissionRegistry.requireModuleRoots(
                Map.of(PermissionModule.SALES_QUOTES,
                        Set.of(PermissionAction.CREATE,
                                PermissionAction.EDIT))))
                .isInstanceOf(InvalidPermissionDependencyException.class)
                .hasMessage(
                        "SALES_QUOTES_CREATE requiere SALES_QUOTES_VIEW_LIST")
                .satisfies(exception -> assertThat(
                        ((InvalidPermissionDependencyException) exception)
                                .missing())
                        .containsExactly("SALES_QUOTES_VIEW_LIST"));
    }

    @Test
    void accepts_view_list_alone_or_with_child_permissions() {
        assertThatCode(() -> PermissionRegistry.requireModuleRoots(
                Map.of(PermissionModule.SALES_QUOTES,
                        Set.of(PermissionAction.VIEW_LIST))))
                .doesNotThrowAnyException();
        assertThatCode(() -> PermissionRegistry.requireModuleRoots(
                Map.of(PermissionModule.SALES_QUOTES,
                        Set.of(PermissionAction.VIEW_LIST,
                                PermissionAction.CREATE,
                                PermissionAction.VIEW_DETAIL))))
                .doesNotThrowAnyException();
    }

    @Test
    void view_detail_is_optional_and_never_implies_other_permissions() {
        assertThatCode(() -> PermissionRegistry.requireModuleRoots(
                Map.of(PermissionModule.SALES_QUOTES,
                        Set.of(PermissionAction.VIEW_LIST,
                                PermissionAction.VIEW_DETAIL))))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> PermissionRegistry.requireModuleRoots(
                Map.of(PermissionModule.SALES_QUOTES,
                        Set.of(PermissionAction.VIEW_DETAIL))))
                .isInstanceOf(InvalidPermissionDependencyException.class);
    }

    @Test
    void modules_without_view_list_need_no_root_dependency() {
        assertThatCode(() -> PermissionRegistry.requireModuleRoots(
                Map.of(PermissionModule.DASHBOARD,
                        Set.of(PermissionAction.VIEW),
                        PermissionModule.COMPANY,
                        Set.of(PermissionAction.EDIT))))
                .doesNotThrowAnyException();
    }
}
