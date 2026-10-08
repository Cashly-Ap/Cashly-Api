package com.cashly.cashly_api.notifications.infrastructure.integration;

import com.cashly.cashly_api.auth.user.application.api.NotificationRecipientsQuery;
import com.cashly.cashly_api.notifications.application.contracts.NotificationRecipientGateway;
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
