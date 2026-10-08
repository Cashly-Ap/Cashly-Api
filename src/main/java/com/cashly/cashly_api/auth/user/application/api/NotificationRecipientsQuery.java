package com.cashly.cashly_api.auth.user.application.api;

import java.util.List;
import java.util.UUID;

public interface NotificationRecipientsQuery {
    List<UUID> findAllUserIds();
}
