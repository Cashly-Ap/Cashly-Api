package com.cashly.cashly_api.notifications.application.contracts;

import com.cashly.cashly_api.notifications.domain.entities.Notification;

public interface NotificationDeliveryGateway {
    void deliver(Notification notification);
}
