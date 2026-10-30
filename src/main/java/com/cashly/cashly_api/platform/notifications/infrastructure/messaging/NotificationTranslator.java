package com.cashly.cashly_api.platform.notifications.infrastructure.messaging;

import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator;
import java.util.Locale;

public final class NotificationTranslator {
    private final MessageTranslator messages = new MessageTranslator();
    public Notification render(Notification notification, Locale locale) {
        if (notification.getTitleKey() == null || notification.getMessageKey() == null) return notification;
        return Notification.reconstitute(notification.getId(), notification.getUserId(), notification.getCustomerId(),
                notification.getType(), messages.translate(notification.getTitleKey(), locale),
                messages.translate(notification.getMessageKey(), locale, notification.getMessageArguments().toArray()),
                notification.getReferenceType(), notification.getReferenceId(), notification.isRead(),
                notification.getReadAt(), notification.getCreatedAt());
    }
}
