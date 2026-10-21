package com.cashly.cashly_api.platform.auth.authorization.domain.services;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.BusinessModule;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;

public final class PermissionRegistry  {
    private PermissionRegistry()  {
    }
    public static Map<BusinessModule, Set<PermissionAction>> modules()  {
        var result = new EnumMap<BusinessModule, Set<PermissionAction>>(BusinessModule.class);
        for (var module : BusinessModule.values())  {
            var actions = EnumSet.of(PermissionAction.READ, PermissionAction.CREATE, PermissionAction.UPDATE, PermissionAction.DELETE);
            if (Set.of(BusinessModule.CATALOG, BusinessModule.INVENTORY, BusinessModule.SALES, BusinessModule.FINANCES, BusinessModule.PURCHASES, BusinessModule.DOCUMENT_ADMIN).contains(module)) actions.add(PermissionAction.EXPORT);
            if (Set.of(BusinessModule.INVENTORY, BusinessModule.SALES, BusinessModule.FINANCES, BusinessModule.PURCHASES, BusinessModule.DOCUMENT_ADMIN).contains(module)) actions.add(PermissionAction.EXECUTE);
            if (module == BusinessModule.DOCUMENT_ADMIN) actions = EnumSet.of(PermissionAction.READ, PermissionAction.EXPORT);
            if (module == BusinessModule.COMPANY) actions = EnumSet.of(PermissionAction.READ, PermissionAction.UPDATE);
            if (module == BusinessModule.DASHBOARD) actions = EnumSet.of(PermissionAction.READ);
            result.put(module, Collections.unmodifiableSet(actions));
        }
        return Collections.unmodifiableMap(result);
    }
    public static Map<BusinessModule, Set<PermissionAction>> validate(Map<BusinessModule, Set<PermissionAction>> grants)  {
        if (grants == null) throw new IllegalArgumentException("Permissions are required");
        var result = new EnumMap<BusinessModule, Set<PermissionAction>>(BusinessModule.class);
        grants.forEach((module, actions) -> {
            if (module == null || actions == null || !modules().get(module).containsAll(actions)) throw new IllegalArgumentException("Unsupported permission");
            if (!actions.isEmpty() && !actions.contains(PermissionAction.READ)) throw new IllegalArgumentException("READ is required before other actions");
            if (!actions.isEmpty()) result.put(module, Set.copyOf(actions));
        });
        return Collections.unmodifiableMap(result);
    }
}
