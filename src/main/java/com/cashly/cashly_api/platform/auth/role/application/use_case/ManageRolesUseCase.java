package com.cashly.cashly_api.platform.auth.role.application.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessReferenceNotFoundException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ManageRolesUseCase {
    private final AccessRepository repository;
    private final AccessService access;

    public ManageRolesUseCase(AccessRepository repository, AccessService access) {
        this.repository = repository;
        this.access = access;
    }

    public record Request(String name, String description, Boolean active, Long version,
                          Map<BusinessModule, Set<PermissionAction>> permissions) {}
    public record Option(UUID id, String name) {}

    public List<Role> list() {
        access.require(BusinessModule.ROLES, PermissionAction.READ);
        return repository.roles();
    }

    public Role find(UUID id) {
        access.require(BusinessModule.ROLES, PermissionAction.READ);
        return existing(id);
    }

    public List<Option> options() {
        requireAssignment();
        return repository.roles().stream().filter(Role::active).filter(this::assignable)
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
        var permissions = access.current().permissions().getOrDefault(BusinessModule.USERS, Set.of());
        if (!permissions.contains(PermissionAction.READ)
                || (!permissions.contains(PermissionAction.CREATE) && !permissions.contains(PermissionAction.UPDATE))) {
            throw new AccessDeniedException("User administration required");
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Role save(UUID id, Request request) {
        repository.lockAdministration();
        boolean create = id == null;
        access.require(BusinessModule.ROLES, create ? PermissionAction.CREATE : PermissionAction.UPDATE);
        if (!create && existing(id).systemAdministrator()) {
            throw new AccessConflictException("Protected system role cannot be changed");
        }
        if (!create && request.version() == null) {
            throw new IllegalArgumentException("Version is required");
        }
        var role = new Role(create ? UUID.randomUUID() : id, request.name(), request.description(),
                request.active() == null || request.active(), create ? 0 : request.version(), null, request.permissions());
        if (repository.roles().stream().anyMatch(other -> !other.id().equals(role.id())
                && other.name().equalsIgnoreCase(role.name()))) {
            throw new AccessConflictException("Role name already exists");
        }
        if (!role.active() && repository.roleReferenced(role.id())) {
            throw new AccessConflictException("Reassign users before deactivating role");
        }
        access.delegable(role);
        repository.saveRole(role, create);
        return existing(role.id());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(UUID id) {
        repository.lockAdministration();
        access.require(BusinessModule.ROLES, PermissionAction.DELETE);
        var role = existing(id);
        if (role.systemAdministrator() || repository.roleReferenced(id)) {
            throw new AccessConflictException("Protected or referenced role cannot be deleted");
        }
        access.delegable(role);
        repository.deleteRole(id);
    }

    private Role existing(UUID id) {
        return repository.role(id).orElseThrow(() -> new AccessReferenceNotFoundException("Role not found"));
    }
}
