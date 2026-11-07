package com.cashly.cashly_api.platform.notifications.domain.entity;

import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationId;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

public class Notification {

    private final NotificationId id;
    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private String titleKey;
    private String messageKey;
    private java.util.List<String> messageArguments = java.util.List.of();
    private final String referenceType;
    private final UUID referenceId;
    private boolean isRead;
    private LocalDateTime readAt;
    private final LocalDateTime createdAt;

    private Notification(NotificationId id, UUID userId, NotificationType type,
            String title, String message, String referenceType,
            UUID referenceId, boolean isRead, LocalDateTime readAt,
            LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public static Notification create(UUID userId, NotificationType type,
            String title, String message, String referenceType,
            UUID referenceId) {
        Objects.requireNonNull(userId, "userId must be provided");
        Objects.requireNonNull(type, "Notification type cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        return new Notification(NotificationId.generate(), userId, type, title,
                message, referenceType, referenceId, false, null,
                LocalDateTime.now(ZoneOffset.UTC));
    }

    public static Notification reconstitute(NotificationId id, UUID userId,
            NotificationType type, String title, String message,
            String referenceType, UUID referenceId, boolean isRead,
            LocalDateTime readAt, LocalDateTime createdAt) {
        return new Notification(id, userId, type, title, message, referenceType,
                referenceId, isRead, readAt, createdAt);
    }

    public Notification withTranslation(String titleKey, String messageKey,
            java.util.List<String> arguments) {
        this.titleKey = titleKey;
        this.messageKey = messageKey;
        this.messageArguments = arguments == null
                ? java.util.List.of()
                : java.util.List.copyOf(arguments);
        return this;
    }
    public String getTitleKey() {
        return titleKey;
    }
    public String getMessageKey() {
        return messageKey;
    }
    public java.util.List<String> getMessageArguments() {
        return messageArguments;
    }

    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public NotificationId getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", type=" + type + ", title=" + title
                + "}";
    }
}
