package com.cashly.cashly_api.platform.notifications.application.ports.out;

import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;

public interface NotificationDeliveryGateway {
    void deliver(Notification notification);
}
