package com.cashly.cashly_api.platform.auth.role.application.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.domain.exceptions.InvalidPermissionDependencyException;
import com.cashly.cashly_api.platform.auth.authorization.domain.exceptions.UnknownPermissionCodeException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManageRolesUseCaseTest {
    private final AccessRepository repository = mock(AccessRepository.class);
    private final AccessService access = mock(AccessService.class);
    private final ManageRolesUseCase useCase = new ManageRolesUseCase(
            repository, access);
    private final UUID id = UUID.randomUUID();

    private Role role(String key) {
        return new Role(id, "Finance", null, true, 2, key, Map.of());
    }

    @Test
    void protects_system_role_and_referenced_role_lifecycle_without_partial_save() {
        when(repository.role(id)).thenReturn(Optional.of(role("SYSTEM_ADMIN")));
        var update = new ManageRolesUseCase.Request("Changed", null, true, 2L,
                Set.of());
        assertThatThrownBy(() -> useCase.save(id, update))
                .isInstanceOf(AccessConflictException.class);
        assertThatThrownBy(() -> useCase.delete(id))
                .isInstanceOf(AccessConflictException.class);
        when(repository.role(id)).thenReturn(Optional.of(role(null)));
        when(repository.roles()).thenReturn(List.of(role(null)));
        when(repository.roleReferenced(id)).thenReturn(true);
        assertThatThrownBy(
                () -> useCase.save(id,
                        new ManageRolesUseCase.Request("Finance", null, false,
                                2L, Set.of())))
                .isInstanceOf(AccessConflictException.class);
        assertThatThrownBy(() -> useCase.delete(id))
                .isInstanceOf(AccessConflictException.class);
        verify(repository, never()).saveRole(any(), anyBoolean());
        verify(repository, never()).deleteRole(any());
    }

    @Test
    void rejects_stale_version_and_never_swallows_repository_conflict() {
        when(repository.role(id)).thenReturn(Optional.of(role(null)));
        when(repository.roles()).thenReturn(List.of(role(null)));
        doThrow(new AccessConflictException("Stale version")).when(repository)
                .saveRole(any(), eq(false));
        assertThatThrownBy(
                () -> useCase.save(id,
                        new ManageRolesUseCase.Request("Finance", null, true,
                                1L, Set.of())))
                .isInstanceOf(AccessConflictException.class);
    }

    @Test
    void cannotStripPermissionsFromAnExistingRoleAboveTheActorScope() {
        var actorId = UUID.randomUUID();
        var actorRoleId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var actorRole = new Role(actorRoleId, "Limited administrator", null,
                true, 0, null, Map.of(PermissionModule.ROLES, Set
                        .of(PermissionAction.VIEW_LIST, PermissionAction.EDIT)));
        var superior = new Role(id, "Sales administrator", null, true, 2, null,
                Map.of(PermissionModule.SALES_ORDERS,
                        Set.of(PermissionAction.VIEW_LIST,
                                PermissionAction.EDIT)));
        when(repository.assignment(actorId)).thenReturn(
                Optional.of(new AccessRepository.Assignment(actorRoleId,
                        departmentId, true)));
        when(repository.role(actorRoleId)).thenReturn(Optional.of(actorRole));
        when(repository.role(id)).thenReturn(Optional.of(superior));
        var secured = new ManageRolesUseCase(repository,
                new AccessService(repository, () -> actorId));

        assertThatThrownBy(() -> secured.save(id,
                new ManageRolesUseCase.Request("Stripped", null, true, 2L,
                        Set.of())))
                .isInstanceOf(
                        org.springframework.security.access.AccessDeniedException.class);

        verify(repository).lockAdministration();
        verify(repository, never()).saveRole(any(), anyBoolean());
    }

    @Test
    void assignment_options_need_only_user_administration_and_filter_privilege_ceiling() {
        var allowed = role(null);
        var protectedRole = new Role(UUID.randomUUID(), "Recovery", null, true,
                0, "SYSTEM_ADMIN", Map.of());
        when(repository.roles()).thenReturn(List.of(allowed, protectedRole));
        when(access.current()).thenReturn(new AccessService.CurrentAccess(id,
                "Actor", UUID.randomUUID(), false,
                Set.of("USERS_VIEW_LIST", "USERS_CREATE")));
        doThrow(new org.springframework.security.access.AccessDeniedException(
                "Not assignable")).when(access).delegable(protectedRole);
        assertThat(useCase.options())
                .containsExactly(new ManageRolesUseCase.Option(id, "Finance"));
        verify(access, never()).require(PermissionModule.ROLES,
                PermissionAction.VIEW_LIST);
    }

    @Test
    void editing_selector_only_returns_editable_roles_within_actor_scope() {
        var allowed = role(null);
        var superior = new Role(UUID.randomUUID(), "Superior", null, true, 0,
                null, Map.of());
        when(repository.editableRoles()).thenReturn(List.of(allowed, superior));
        doThrow(new org.springframework.security.access.AccessDeniedException(
                "Not delegable")).when(access).delegable(superior);

        assertThat(useCase.list(ManageRolesUseCase.ListAction.EDIT))
                .containsExactly(allowed);
        verify(access).require(PermissionModule.ROLES, PermissionAction.EDIT);
        verify(repository, never()).roles();
    }

    @Test
    void rejects_a_role_whose_children_lack_the_module_view_list_root() {
        when(repository.role(any())).thenReturn(Optional.of(role(null)));
        when(repository.roles()).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.save(null,
                new ManageRolesUseCase.Request("Sales", null, true, null,
                        Set.of("SALES_QUOTES_CREATE",
                                "SALES_QUOTES_EDIT"))))
                .isInstanceOf(InvalidPermissionDependencyException.class)
                .hasMessage(
                        "SALES_QUOTES_CREATE requiere SALES_QUOTES_VIEW_LIST");

        verify(repository, never()).saveRole(any(), anyBoolean());
    }

    @Test
    void refuses_to_strip_the_root_from_an_existing_role_that_keeps_children() {
        var existing = new Role(id, "Finance", null, true, 2, null,
                Map.of(PermissionModule.SALES_QUOTES,
                        Set.of(PermissionAction.VIEW_LIST,
                                PermissionAction.CREATE)));
        when(repository.role(id)).thenReturn(Optional.of(existing));
        when(repository.roles()).thenReturn(List.of(existing));

        assertThatThrownBy(() -> useCase.save(id,
                new ManageRolesUseCase.Request("Finance", null, true, 2L,
                        Set.of("SALES_QUOTES_CREATE"))))
                .isInstanceOf(InvalidPermissionDependencyException.class);

        verify(repository, never()).saveRole(any(), anyBoolean());
    }

    @Test
    void accepts_view_list_alone_and_view_list_with_children() {
        when(repository.role(any())).thenReturn(Optional.of(role(null)));
        when(repository.roles()).thenReturn(List.of());

        useCase.save(null, new ManageRolesUseCase.Request("Read only", null,
                true, null, Set.of("SALES_QUOTES_VIEW_LIST")));
        useCase.save(null, new ManageRolesUseCase.Request("Sales", null, true,
                null, Set.of("SALES_QUOTES_VIEW_LIST",
                        "SALES_QUOTES_CREATE")));

        verify(repository, times(2)).saveRole(any(), eq(true));
    }

    @Test
    void rejects_unknown_permission_codes_before_touching_storage() {
        assertThatThrownBy(() -> useCase.save(null,
                new ManageRolesUseCase.Request("Sales", null, true, null,
                        Set.of("SALES_QUOTES_TELEPORT"))))
                .isInstanceOf(UnknownPermissionCodeException.class);

        verify(repository, never()).saveRole(any(), anyBoolean());
    }

}
