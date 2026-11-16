package com.cashly.cashly_api.platform.auth.authorization.application.services;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessReferenceNotFoundException;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AccessService {
    private final AccessRepository repository;
    private final CurrentActor actor;

    public AccessService(AccessRepository repository, CurrentActor actor) {
        this.repository = repository;
        this.actor = actor;
    }

    public record CurrentAccess(UUID roleId, String roleName, UUID departmentId,
            boolean systemAdministrator, Set<String> permissions) {
    }

    public CurrentAccess resolve(UUID userId) {
        var assignment = repository.assignment(userId)
                .filter(AccessRepository.Assignment::active).orElseThrow(
                        () -> new AccessDeniedException("Account unavailable"));
        var role = repository.role(assignment.roleId()).filter(Role::active)
                .orElseThrow(
                        () -> new AccessDeniedException("Role unavailable"));
        return new CurrentAccess(role.id(), role.name(),
                assignment.departmentId(), role.systemAdministrator(),
                role.codes());
    }

    public CurrentAccess current() {
        return resolve(actor.requireId());
    }

    public void require(PermissionModule module, PermissionAction action) {
        require(module.name() + "_" + action.name());
    }

    public void require(String permissionCode) {
        if (!current().permissions().contains(permissionCode)) {
            throw new AccessDeniedException("Missing " + permissionCode);
        }
    }

    public void delegable(Role role) {
        var actor = current();
        if (role.systemAdministrator() && !actor.systemAdministrator()) {
            throw new AccessDeniedException(
                    "Only a system administrator can assign the protected role");
        }
        delegable(role.effectivePermissions());
    }

    public void delegable(
            Map<PermissionModule, Set<PermissionAction>> requested) {
        var actor = current();
        requested.forEach((module, actions) -> actions.forEach(action -> {
            var code = module.name() + "_" + action.name();
            if (!actor.permissions().contains(code)) {
                throw new AccessDeniedException(
                        "Cannot delegate permissions you do not possess");
            }
        }));
    }

    public void lockAdministration() {
        repository.lockAdministration();
    }

    public void validateAssignment(UUID roleId, UUID departmentId) {
        repository.lockAdministration();
        if (roleId == null || departmentId == null) {
            throw new IllegalArgumentException(
                    "Role and department are required");
        }
        var role = repository.role(roleId).orElseThrow(
                () -> new AccessReferenceNotFoundException("Role not found"));
        var department = repository.department(departmentId)
                .orElseThrow(() -> new AccessReferenceNotFoundException(
                        "Department not found"));
        if (!role.active() || !department.active()) {
            throw new AccessConflictException(
                    "Select active role and department");
        }
        delegable(role);
    }

    public void authorizeAccountMutation(UUID targetId,
            boolean allowSelfService) {
        repository.lockAdministration();
        if (allowSelfService && targetId.equals(actor.requireId())) {
            current();
            return;
        }
        require(PermissionModule.USERS, PermissionAction.EDIT);
        var target = repository.assignment(targetId).orElseThrow(
                () -> new AccessReferenceNotFoundException("User not found"));
        delegable(repository.role(target.roleId()).orElseThrow(
                () -> new AccessReferenceNotFoundException("Role not found")));
    }

    public void protectAdministrator(UUID userId, UUID nextRoleId,
            boolean nextActive) {
        repository.lockAdministration();
        var existing = repository.assignment(userId).orElse(null);
        if (existing == null) {
            return;
        }
        var oldRole = repository.role(existing.roleId()).orElseThrow();
        delegable(oldRole);
        if (!existing.active() || !oldRole.systemAdministrator()) {
            return;
        }
        boolean retains = nextActive && nextRoleId != null && repository
                .role(nextRoleId).map(Role::systemAdministrator).orElse(false);
        if (!retains && repository.activeSystemAdministrators() <= 1) {
            throw new AccessConflictException(
                    "At least one active system administrator must remain");
        }
    }
}
