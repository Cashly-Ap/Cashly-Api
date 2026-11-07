package com.cashly.cashly_api.platform.notifications.application.ports.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionAccessGateway {

    Access resolve(UUID userId);

    String moduleLabelKey(String module);

    List<String> notificationEligibleModules();

    record Access(boolean systemAdministrator, Set<String> permissions) {
    }
}
