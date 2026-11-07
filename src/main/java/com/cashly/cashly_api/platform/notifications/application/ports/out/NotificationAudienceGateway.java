package com.cashly.cashly_api.platform.notifications.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface NotificationAudienceGateway {
    List<UUID> findRecipients(String module, String action,
            UUID excludeUserId);
}
