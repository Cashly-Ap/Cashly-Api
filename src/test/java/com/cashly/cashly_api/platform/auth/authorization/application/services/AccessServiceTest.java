package com.cashly.cashly_api.platform.auth.authorization.application.services;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessReferenceNotFoundException;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessServiceTest {
    private final AccessRepository repository = mock(AccessRepository.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID roleId = UUID.randomUUID();
    private final UUID departmentId = UUID.randomUUID();
    private final AccessService service = new AccessService(repository, () -> userId);

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null, Set.of()));
        when(repository.assignment(userId)).thenReturn(Optional.of(
                new AccessRepository.Assignment(roleId, departmentId, true)));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Role role(Map<BusinessModule, Set<PermissionAction>> permissions) {
        return new Role(roleId, "Custom", null, true, 0, null, permissions);
    }

    @ParameterizedTest
    @EnumSource(BusinessModule.class)
    void every_module_revokes_on_next_resolution_without_trusting_session_claims(BusinessModule module) {
        when(repository.role(roleId)).thenReturn(
                Optional.of(role(Map.of(module, Set.of(PermissionAction.READ)))),
                Optional.of(role(Map.of())));
        service.require(module, PermissionAction.READ);
        assertThatThrownBy(() -> service.require(module, PermissionAction.READ))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void validates_active_role_and_department_under_the_administration_lock() {
        when(repository.role(roleId)).thenReturn(Optional.of(role(Map.of(
                BusinessModule.USERS, Set.of(PermissionAction.READ)))));
        when(repository.department(departmentId)).thenReturn(Optional.of(
                new Department(departmentId, "Operations", null, true, 0)));

        service.validateAssignment(roleId, departmentId);

        var order = inOrder(repository);
        order.verify(repository).lockAdministration();
        order.verify(repository).role(roleId);
        order.verify(repository).department(departmentId);
    }

    @Test
    void rejects_missing_or_inactive_role_and_department_assignments() {
        assertThatThrownBy(() -> service.validateAssignment(null, departmentId))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository).lockAdministration();

        when(repository.role(roleId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.validateAssignment(roleId, departmentId))
                .isInstanceOf(AccessReferenceNotFoundException.class);

        when(repository.role(roleId)).thenReturn(Optional.of(new Role(roleId, "Inactive", null,
                false, 0, null, Map.of())));
        assertThatThrownBy(() -> service.validateAssignment(roleId, departmentId))
                .isInstanceOf(AccessReferenceNotFoundException.class);

        when(repository.role(roleId)).thenReturn(Optional.of(role(Map.of())));
        when(repository.department(departmentId)).thenReturn(Optional.of(
                new Department(departmentId, "Inactive", null, false, 0)));
        assertThatThrownBy(() -> service.validateAssignment(roleId, departmentId))
                .isInstanceOf(AccessConflictException.class);
    }

    @Test
    void rejects_assigning_a_role_with_permissions_above_the_actor_scope() {
        when(repository.role(roleId)).thenReturn(Optional.of(role(Map.of(
                BusinessModule.USERS, Set.of(PermissionAction.READ)))));
        var elevatedRoleId = UUID.randomUUID();
        when(repository.role(elevatedRoleId)).thenReturn(Optional.of(new Role(elevatedRoleId,
                "Elevated", null, true, 0, null, Map.of(BusinessModule.USERS,
                Set.of(PermissionAction.READ, PermissionAction.UPDATE)))));
        when(repository.department(departmentId)).thenReturn(Optional.of(
                new Department(departmentId, "Operations", null, true, 0)));

        assertThatThrownBy(() -> service.validateAssignment(elevatedRoleId, departmentId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejects_role_deactivation_and_delegation_above_actor_scope() {
        when(repository.role(roleId)).thenReturn(Optional.of(role(Map.of(
                BusinessModule.CATALOG, Set.of(PermissionAction.READ)))));
        assertThatThrownBy(() -> service.delegable(Map.of(BusinessModule.CATALOG,
                Set.of(PermissionAction.READ, PermissionAction.UPDATE))))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.delegable(new Role(UUID.randomUUID(), "Admin", null,
                true, 0, "SYSTEM_ADMIN", Map.of())))
                .isInstanceOf(AccessDeniedException.class);
        when(repository.role(roleId)).thenReturn(Optional.of(new Role(roleId, "Disabled", null,
                false, 0, null, Map.of())));
        assertThatThrownBy(() -> service.resolve(userId)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void serializes_and_rejects_removal_of_last_protected_administrator() {
        when(repository.role(roleId)).thenReturn(Optional.of(new Role(roleId, "Recovery", null,
                true, 0, "SYSTEM_ADMIN", Map.of())));
        when(repository.activeSystemAdministrators()).thenReturn(1L);
        assertThatThrownBy(() -> service.protectAdministrator(userId, null, false))
                .isInstanceOf(AccessConflictException.class);
        var order = inOrder(repository);
        order.verify(repository).lockAdministration();
        order.verify(repository).assignment(userId);
        verify(repository).activeSystemAdministrators();
    }
    @Test
    void delegated_administrator_cannot_reset_credentials_or_delete_inactive_superior_account() {
        var targetId = UUID.randomUUID();
        var protectedRoleId = UUID.randomUUID();
        when(repository.role(roleId)).thenReturn(Optional.of(role(Map.of(BusinessModule.USERS,
                Set.of(PermissionAction.READ, PermissionAction.UPDATE, PermissionAction.DELETE)))));
        when(repository.assignment(targetId)).thenReturn(Optional.of(
                new AccessRepository.Assignment(protectedRoleId, departmentId, false)));
        when(repository.role(protectedRoleId)).thenReturn(Optional.of(new Role(protectedRoleId,
                "Recovery", null, true, 0, "SYSTEM_ADMIN", Map.of())));
        assertThatThrownBy(() -> service.authorizeAccountMutation(targetId, false))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.protectAdministrator(targetId, null, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void own_password_service_needs_no_user_administration_but_other_accounts_do() {
        when(repository.role(roleId)).thenReturn(Optional.of(role(Map.of())));
        service.authorizeAccountMutation(userId, true);
        assertThatThrownBy(() -> service.authorizeAccountMutation(UUID.randomUUID(), true))
                .isInstanceOf(AccessDeniedException.class);
    }

}
