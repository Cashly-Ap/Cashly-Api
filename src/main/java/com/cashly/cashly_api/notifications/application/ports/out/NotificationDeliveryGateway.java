package com.cashly.cashly_api.notifications.application.ports.out;

import com.cashly.cashly_api.notifications.domain.entities.Notification;

public interface NotificationDeliveryGateway {
    void deliver(Notification notification);
}
