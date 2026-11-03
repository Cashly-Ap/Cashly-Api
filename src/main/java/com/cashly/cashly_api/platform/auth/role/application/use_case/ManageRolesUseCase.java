package com.cashly.cashly_api.platform.auth.role.application.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessReferenceNotFoundException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionCode;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ManageRolesUseCase {
    private final AccessRepository repository;
    private final AccessService access;

    public ManageRolesUseCase(AccessRepository repository,
            AccessService access) {
        this.repository = repository;
        this.access = access;
    }

    public record Request(String name, String description, Boolean active,
            Long version, Set<String> permissions) {
    }

    public record Option(UUID id, String name) {
    }

    public enum ListAction {
        EDIT, DELETE
    }

    public List<Role> list() {
        return list(null);
    }

    public List<Role> list(ListAction action) {
        access.require(PermissionModule.ROLES, PermissionAction.VIEW_LIST);
        if (action == ListAction.EDIT) {
            access.require(PermissionModule.ROLES, PermissionAction.EDIT);
            return repository.editableRoles().stream().filter(this::assignable)
                    .toList();
        }
        if (action == ListAction.DELETE) {
            access.require(PermissionModule.ROLES, PermissionAction.DELETE);
            return repository.roles().stream()
                    .filter(role -> !role.systemAdministrator())
                    .filter(role -> !repository.roleReferenced(role.id()))
                    .filter(this::assignable)
                    .toList();
        }
        return repository.roles();
    }

    public Role find(UUID id) {
        access.require(PermissionModule.ROLES, PermissionAction.VIEW_DETAIL);
        return existing(id);
    }

    public List<Option> options() {
        requireAssignment();
        return repository.roles().stream().filter(Role::active)
                .filter(this::assignable)
                .map(role -> new Option(role.id(), role.name())).toList();
    }

    private boolean assignable(Role role) {
        try {
            access.delegable(role);
            return true;
        } catch (AccessDeniedException exception) {
            return false;
        }
    }

    private void requireAssignment() {
        var permissions = access.current().permissions();
        if (!permissions.contains("USERS_VIEW_LIST")
                || (!permissions.contains("USERS_CREATE")
                        && !permissions.contains("USERS_EDIT"))) {
            throw new AccessDeniedException("User administration required");
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Role save(UUID id, Request request) {
        repository.lockAdministration();
        boolean create = id == null;
        access.require(PermissionModule.ROLES, create
                ? PermissionAction.CREATE
                : PermissionAction.EDIT);
        if (!create) {
            var previous = existing(id);
            if (previous.systemAdministrator()) {
                throw new AccessConflictException(
                        "Protected system role cannot be changed");
            }
            access.delegable(previous);
        }
        if (!create && request.version() == null) {
            throw new IllegalArgumentException("Version is required");
        }
        var role = new Role(create ? UUID.randomUUID() : id, request.name(),
                request.description(),
                request.active() == null || request.active(),
                create ? 0 : request.version(), null,
                permissions(request.permissions()));
        if (repository.roles().stream()
                .anyMatch(other -> !other.id().equals(role.id())
                        && other.name().equalsIgnoreCase(role.name()))) {
            throw new AccessConflictException("Role name already exists");
        }
        if (!role.active() && repository.roleReferenced(role.id())) {
            throw new AccessConflictException(
                    "Reassign users before deactivating role");
        }
        access.delegable(role);
        repository.saveRole(role, create);
        return existing(role.id());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(UUID id) {
        repository.lockAdministration();
        access.require(PermissionModule.ROLES, PermissionAction.DELETE);
        var role = existing(id);
        if (role.systemAdministrator() || repository.roleReferenced(id)) {
            throw new AccessConflictException(
                    "Protected or referenced role cannot be deleted");
        }
        access.delegable(role);
        repository.deleteRole(id);
    }

    private Map<PermissionModule, Set<PermissionAction>> permissions(
            Set<String> codes) {
        var result = new EnumMap<PermissionModule, Set<PermissionAction>>(
                PermissionModule.class);
        if (codes == null) {
            return result;
        }
        codes.forEach(code -> {
            var permission = PermissionCode.parse(code);
            result.computeIfAbsent(permission.module(),
                    key -> EnumSet.noneOf(PermissionAction.class))
                    .add(permission.action());
        });
        return result;
    }

    private Role existing(UUID id) {
        return repository.role(id).orElseThrow(
                () -> new AccessReferenceNotFoundException("Role not found"));
    }
}
