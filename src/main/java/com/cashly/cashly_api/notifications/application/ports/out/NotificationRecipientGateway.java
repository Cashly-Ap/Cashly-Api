package com.cashly.cashly_api.notifications.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface NotificationRecipientGateway {
    List<UUID> findAllUserIds();
}
