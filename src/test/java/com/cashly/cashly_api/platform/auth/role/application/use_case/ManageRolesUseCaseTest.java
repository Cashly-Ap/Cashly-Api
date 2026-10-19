package com.cashly.cashly_api.platform.auth.role.application.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
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
    private final ManageRolesUseCase useCase = new ManageRolesUseCase(repository, access);
    private final UUID id = UUID.randomUUID();

    private Role role(String key) {
        return new Role(id, "Finance", null, true, 2, key, Map.of());
    }

    @Test
    void protects_system_role_and_referenced_role_lifecycle_without_partial_save() {
        when(repository.role(id)).thenReturn(Optional.of(role("SYSTEM_ADMIN")));
        var update = new ManageRolesUseCase.Request("Changed", null, true, 2L, Map.of());
        assertThatThrownBy(() -> useCase.save(id, update)).isInstanceOf(AccessConflictException.class);
        assertThatThrownBy(() -> useCase.delete(id)).isInstanceOf(AccessConflictException.class);
        when(repository.role(id)).thenReturn(Optional.of(role(null)));
        when(repository.roles()).thenReturn(List.of(role(null)));
        when(repository.roleReferenced(id)).thenReturn(true);
        assertThatThrownBy(() -> useCase.save(id,
                new ManageRolesUseCase.Request("Finance", null, false, 2L, Map.of())))
                .isInstanceOf(AccessConflictException.class);
        assertThatThrownBy(() -> useCase.delete(id)).isInstanceOf(AccessConflictException.class);
        verify(repository, never()).saveRole(any(), anyBoolean());
        verify(repository, never()).deleteRole(any());
    }

    @Test
    void rejects_stale_version_and_never_swallows_repository_conflict() {
        when(repository.role(id)).thenReturn(Optional.of(role(null)));
        when(repository.roles()).thenReturn(List.of(role(null)));
        doThrow(new AccessConflictException("Stale version")).when(repository).saveRole(any(), eq(false));
        assertThatThrownBy(() -> useCase.save(id,
                new ManageRolesUseCase.Request("Finance", null, true, 1L, Map.of())))
                .isInstanceOf(AccessConflictException.class);
    }

    @Test
    void cannotStripPermissionsFromAnExistingRoleAboveTheActorScope() {
        var actorId = UUID.randomUUID();
        var actorRoleId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        var actorRole = new Role(actorRoleId, "Limited administrator", null, true, 0, null,
                Map.of(BusinessModule.ROLES, Set.of(PermissionAction.READ, PermissionAction.UPDATE)));
        var superior = new Role(id, "Sales administrator", null, true, 2, null,
                Map.of(BusinessModule.SALES, Set.of(PermissionAction.READ, PermissionAction.UPDATE)));
        when(repository.assignment(actorId)).thenReturn(Optional.of(
                new AccessRepository.Assignment(actorRoleId, departmentId, true)));
        when(repository.role(actorRoleId)).thenReturn(Optional.of(actorRole));
        when(repository.role(id)).thenReturn(Optional.of(superior));
        var secured = new ManageRolesUseCase(repository, new AccessService(repository, () -> actorId));

        assertThatThrownBy(() -> secured.save(id,
                new ManageRolesUseCase.Request("Stripped", null, true, 2L, Map.of())))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        verify(repository).lockAdministration();
        verify(repository, never()).saveRole(any(), anyBoolean());
    }

    @Test
    void assignment_options_need_only_user_administration_and_filter_privilege_ceiling() {
        var allowed = role(null);
        var protectedRole = new Role(UUID.randomUUID(), "Recovery", null, true, 0, "SYSTEM_ADMIN", Map.of());
        when(repository.roles()).thenReturn(List.of(allowed, protectedRole));
        when(access.current()).thenReturn(new AccessService.CurrentAccess(id, "Actor", UUID.randomUUID(), false,
                Map.of(BusinessModule.USERS, Set.of(PermissionAction.READ, PermissionAction.CREATE))));
        doThrow(new org.springframework.security.access.AccessDeniedException("Not assignable"))
                .when(access).delegable(protectedRole);
        assertThat(useCase.options()).containsExactly(new ManageRolesUseCase.Option(id, "Finance"));
        verify(access, never()).require(BusinessModule.ROLES, PermissionAction.READ);
    }
}
