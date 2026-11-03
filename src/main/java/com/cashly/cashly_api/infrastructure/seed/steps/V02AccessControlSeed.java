package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class V02AccessControlSeed implements SeedStep {

    private final AccessRepository access;

    public V02AccessControlSeed(AccessRepository access) {
        this.access = access;
    }

    @Override
    public int version() {
        return 2;
    }

    @Override
    public void seed() {
        saveRole(new Role(SYSTEM_ADMIN_ROLE, "System administrator",
                "Full installation access", true, 0, "SYSTEM_ADMIN",
                Map.of()));
        saveRole(new Role(OPERATOR_ROLE, "Operator", "Daily sales operations",
                true, 0, "OPERATOR", operatorPermissions()));
        saveDepartment(new Department(ADMINISTRATION_DEPARTMENT,
                "Administration", "Back office", true, 0));
        saveDepartment(new Department(SALES_DEPARTMENT, "Sales",
                "Commercial operations", true, 0));
        access.createAdministrationLockIfAbsent();
    }

    private void saveRole(Role role) {
        if (access.role(role.id()).isEmpty()) {
            access.saveRole(role, true);
        }
    }

    private void saveDepartment(Department department) {
        if (access.department(department.id()).isEmpty()) {
            access.saveDepartment(department, true);
        }
    }

    private static Map<PermissionModule, Set<PermissionAction>> operatorPermissions() {
        var result = new EnumMap<PermissionModule, Set<PermissionAction>>(
                PermissionModule.class);
        result.put(PermissionModule.SALES_QUOTES, EnumSet.of(
                PermissionAction.VIEW_LIST, PermissionAction.VIEW_DETAIL));
        result.put(PermissionModule.SALES_ORDERS, EnumSet.of(
                PermissionAction.VIEW_LIST, PermissionAction.VIEW_DETAIL));
        result.put(PermissionModule.CONTACTS_CUSTOMERS, EnumSet.of(
                PermissionAction.VIEW_LIST, PermissionAction.VIEW_DETAIL));
        result.put(PermissionModule.FINANCE_TREASURY_RECEIVABLES, EnumSet.of(
                PermissionAction.VIEW_LIST, PermissionAction.VIEW_DEBT,
                PermissionAction.PAY_ALL, PermissionAction.PARTIAL_PAYMENT,
                PermissionAction.PAY_DIRECT,
                PermissionAction.PAY_WITH_VALIDATION));
        return result;
    }
}
