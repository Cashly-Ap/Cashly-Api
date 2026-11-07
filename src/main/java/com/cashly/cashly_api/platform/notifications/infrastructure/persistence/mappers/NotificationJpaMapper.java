package com.cashly.cashly_api.platform.notifications.infrastructure.persistence.mappers;

import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationId;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;
import com.cashly.cashly_api.platform.notifications.infrastructure.persistence.entities.NotificationEntity;

public class NotificationJpaMapper {

    private NotificationJpaMapper() {
    }

    public static NotificationEntity toEntity(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(notification.getId().value());
        entity.setUserId(notification.getUserId());
        entity.setType(notification.getType().name());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setTitleKey(notification.getTitleKey());
        entity.setMessageKey(notification.getMessageKey());
        entity.setMessageArguments(notification.getMessageArguments());
        entity.setReferenceType(notification.getReferenceType());
        entity.setReferenceId(notification.getReferenceId());
        entity.setRead(notification.isRead());
        entity.setReadAt(notification.getReadAt());
        entity.setCreatedAt(notification.getCreatedAt());
        return entity;
    }

    public static Notification toDomain(NotificationEntity entity) {
        return Notification.reconstitute(NotificationId.of(entity.getId()),
                entity.getUserId(),
                NotificationType.valueOf(entity.getType()), entity.getTitle(),
                entity.getMessage(), entity.getReferenceType(),
                entity.getReferenceId(), entity.isRead(), entity.getReadAt(),
                entity.getCreatedAt()).withTranslation(entity.getTitleKey(),
                        entity.getMessageKey(), entity.getMessageArguments());
    }
}
