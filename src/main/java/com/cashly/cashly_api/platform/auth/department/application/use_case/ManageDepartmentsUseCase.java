package com.cashly.cashly_api.platform.auth.department.application.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.audit.application.ports.in.AuditRecorder;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessReferenceNotFoundException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
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

    private final ActivityNotificationRecorder activityNotifications;

    private final AuditRecorder auditRecorder;

    public ManageDepartmentsUseCase(AccessRepository repository,
            AccessService access,
            ActivityNotificationRecorder activityNotifications,
            AuditRecorder auditRecorder) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.auditRecorder = auditRecorder != null
                ? auditRecorder
                : AuditRecorder.NONE;
        this.repository = repository;
        this.access = access;
    }

    public record Request(String name, String description, Boolean active,
            Long version) {
    }
    public record Option(UUID id, String name) {
    }

    public enum ListAction {
        DELETE
    }

    public List<Department> list() {
        return list(null);
    }

    public List<Department> list(ListAction action) {
        access.require(PermissionModule.DEPARTMENTS, PermissionAction.VIEW_LIST);
        if (action == ListAction.DELETE) {
            access.require(PermissionModule.DEPARTMENTS, PermissionAction.DELETE);
            return repository.departments().stream()
                    .filter(department -> !repository.departmentReferenced(department.id()))
                    .toList();
        }
        return repository.departments();
    }

    public Department find(UUID id) {
        access.require(PermissionModule.DEPARTMENTS, PermissionAction.VIEW_DETAIL);
        return existing(id);
    }

    public List<Option> options() {
        var permissions = access.current().permissions();
        if (!permissions.contains("USERS_VIEW_LIST")
                || (!permissions.contains("USERS_CREATE")
                        && !permissions.contains("USERS_EDIT"))) {
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
        access.require(PermissionModule.DEPARTMENTS,
                create ? PermissionAction.CREATE : PermissionAction.EDIT);
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
        activityNotifications.record(ActivityEvent.of("DEPARTMENTS",
                create ? "CREATE" : "EDIT",
                "DEPARTMENT", department.id(), department.name(), null));
        repository.saveDepartment(department, create);
        auditRecorder.record("platform", "DEPARTMENT", department.id(),
                department.name(), create ? "CREATE" : "EDIT");
        return existing(department.id());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(UUID id) {
        repository.lockAdministration();
        access.require(PermissionModule.DEPARTMENTS, PermissionAction.DELETE);
        var department = existing(id);
        if (repository.departmentReferenced(id)) {
            throw new AccessConflictException(
                    "Reassign users before deleting department");
        }
        repository.deleteDepartment(id);
        auditRecorder.record("platform", "DEPARTMENT", department.id(),
                department.name(), "DELETE");
        activityNotifications.record(ActivityEvent.of("DEPARTMENTS",
                "DELETE", "DEPARTMENT", department.id(),
                department.name(), null));
    }

    private Department existing(UUID id) {
        return repository.department(id)
                .orElseThrow(() -> new AccessReferenceNotFoundException(
                        "Department not found"));
    }
}
