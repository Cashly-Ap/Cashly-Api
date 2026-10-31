package com.cashly.cashly_api.platform.auth.department.application.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessReferenceNotFoundException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ManageDepartmentsUseCase {
    private final AccessRepository repository;
    private final AccessService access;

    public ManageDepartmentsUseCase(AccessRepository repository,
            AccessService access) {
        this.repository = repository;
        this.access = access;
    }

    public record Request(String name, String description, Boolean active,
            Long version) {
    }
    public record Option(UUID id, String name) {
    }

    public List<Department> list() {
        access.require(BusinessModule.DEPARTMENTS, PermissionAction.READ);
        return repository.departments();
    }

    public Department find(UUID id) {
        access.require(BusinessModule.DEPARTMENTS, PermissionAction.READ);
        return existing(id);
    }

    public List<Option> options() {
        var permissions = access.current().permissions()
                .getOrDefault(BusinessModule.USERS, Set.of());
        if (!permissions.contains(PermissionAction.READ)
                || (!permissions.contains(PermissionAction.CREATE)
                        && !permissions.contains(PermissionAction.UPDATE))) {
            throw new AccessDeniedException("User administration required");
        }
        return repository.departments().stream().filter(Department::active).map(
                department -> new Option(department.id(), department.name()))
                .toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Department save(UUID id, Request request) {
        repository.lockAdministration();
        boolean create = id == null;
        access.require(BusinessModule.DEPARTMENTS,
                create ? PermissionAction.CREATE : PermissionAction.UPDATE);
        if (!create) {
            existing(id);
        }
        if (!create && request.version() == null) {
            throw new IllegalArgumentException("Version is required");
        }
        var department = new Department(create ? UUID.randomUUID() : id,
                request.name(), request.description(),
                request.active() == null || request.active(),
                create ? 0 : request.version());
        if (repository.departments().stream()
                .anyMatch(other -> !other.id().equals(department.id())
                        && other.name().equalsIgnoreCase(department.name()))) {
            throw new AccessConflictException("Department name already exists");
        }
        if (!department.active()
                && repository.departmentReferenced(department.id())) {
            throw new AccessConflictException(
                    "Reassign users before deactivating department");
        }
        repository.saveDepartment(department, create);
        return existing(department.id());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(UUID id) {
        repository.lockAdministration();
        access.require(BusinessModule.DEPARTMENTS, PermissionAction.DELETE);
        existing(id);
        if (repository.departmentReferenced(id)) {
            throw new AccessConflictException(
                    "Reassign users before deleting department");
        }
        repository.deleteDepartment(id);
    }

    private Department existing(UUID id) {
        return repository.department(id)
                .orElseThrow(() -> new AccessReferenceNotFoundException(
                        "Department not found"));
    }
}
