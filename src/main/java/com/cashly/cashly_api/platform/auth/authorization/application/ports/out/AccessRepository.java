package com.cashly.cashly_api.platform.auth.authorization.application.ports.out;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;

public interface AccessRepository {
    Optional<Role> role(UUID id);
    List<Role> roles();
    List<Role> editableRoles();
    void saveRole(Role role, boolean create);
    void deleteRole(UUID id);
    Optional<Department> department(UUID id);
    List<Department> departments();
    void saveDepartment(Department department, boolean create);
    void deleteDepartment(UUID id);
    boolean roleReferenced(UUID id);
    boolean departmentReferenced(UUID id);
    void createAdministrationLockIfAbsent();
    void lockAdministration();
    long activeSystemAdministrators();
    Optional<Assignment> assignment(UUID userId);
    record Assignment(UUID roleId, UUID departmentId, boolean active) {
    }
}
