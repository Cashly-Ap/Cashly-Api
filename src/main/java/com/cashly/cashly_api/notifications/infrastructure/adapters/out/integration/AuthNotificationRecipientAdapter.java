package com.cashly.cashly_api.notifications.infrastructure.adapters.out.integration;

import com.cashly.cashly_api.auth.user.application.ports.in.NotificationRecipientsQuery;
import com.cashly.cashly_api.notifications.application.ports.out.NotificationRecipientGateway;
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
