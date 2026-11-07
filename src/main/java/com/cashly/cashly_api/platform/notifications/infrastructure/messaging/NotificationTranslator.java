package com.cashly.cashly_api.platform.notifications.infrastructure.messaging;

import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator;
import java.util.List;
import java.util.Locale;

public final class NotificationTranslator {

    private static final String I18N_PREFIX = "i18n:";

    private final MessageTranslator messages = new MessageTranslator();

    public Notification render(Notification notification, Locale locale) {
        if (notification.getTitleKey() == null
                || notification.getMessageKey() == null)
            return notification;
        Object[] arguments = notification.getMessageArguments().stream()
                .map(argument -> resolve(argument, locale)).toArray();
        return Notification.reconstitute(notification.getId(),
                notification.getUserId(), notification.getType(),
                messages.translate(notification.getTitleKey(), locale),
                messages.translate(notification.getMessageKey(), locale,
                        arguments),
                notification.getReferenceType(), notification.getReferenceId(),
                notification.isRead(), notification.getReadAt(),
                notification.getCreatedAt());
    }

    private Object resolve(String argument, Locale locale) {
        if (argument != null && argument.startsWith(I18N_PREFIX)) {
            return messages.translate(argument.substring(I18N_PREFIX.length()),
                    locale);
        }
        return argument;
    }

    public List<String> previewArguments(Notification notification,
            Locale locale) {
        return notification.getMessageArguments().stream()
                .map(argument -> String.valueOf(resolve(argument, locale)))
                .toList();
    }
}
