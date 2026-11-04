package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.OPERATOR_ROLE;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class V30OperatorPaymentMethodsSeed implements SeedStep {

    private static final Map<PermissionModule, Set<PermissionAction>> GRANTS = Map
            .of(PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS, EnumSet
                    .of(PermissionAction.VIEW_LIST,
                            PermissionAction.VIEW_DETAIL));

    private final AccessRepository access;

    public V30OperatorPaymentMethodsSeed(AccessRepository access) {
        this.access = access;
    }

    @Override
    public int version() {
        return 30;
    }

    @Override
    public void seed() {
        var role = access.role(OPERATOR_ROLE).orElse(null);
        if (role == null || role.systemAdministrator()) {
            return;
        }
        var permissions = copy(role.permissions());
        if (!merge(permissions)) {
            return;
        }
        access.saveRole(new Role(role.id(), role.name(), role.description(),
                role.active(), role.version(), role.systemKey(), permissions),
                false);
    }

    private static Map<PermissionModule, Set<PermissionAction>> copy(
            Map<PermissionModule, Set<PermissionAction>> source) {
        var result = new EnumMap<PermissionModule, Set<PermissionAction>>(
                PermissionModule.class);
        source.forEach((module, actions) -> {
            var copy = EnumSet.noneOf(PermissionAction.class);
            copy.addAll(actions);
            result.put(module, copy);
        });
        return result;
    }

    private static boolean merge(
            Map<PermissionModule, Set<PermissionAction>> permissions) {
        boolean changed = false;
        for (var entry : GRANTS.entrySet()) {
            var actions = permissions.computeIfAbsent(entry.getKey(),
                    key -> EnumSet.noneOf(PermissionAction.class));
            changed |= actions.addAll(entry.getValue());
        }
        return changed;
    }
}
