package com.cashly.cashly_api.platform.notifications.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.user.application.ports.in.NotificationRecipientsQuery;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationRecipientGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AuthNotificationRecipientAdapter implements NotificationRecipientGateway {
    private final NotificationRecipientsQuery recipientsQuery;

    public AuthNotificationRecipientAdapter(NotificationRecipientsQuery recipientsQuery) {
        this.recipientsQuery = recipientsQuery;
    }

    @Override
    public List<UUID> findAllUserIds() {
        return recipientsQuery.findAllUserIds();
    }
}
