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
    private final UUID customerId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String referenceType;
    private final UUID referenceId;
    private boolean isRead;
    private LocalDateTime readAt;
    private final LocalDateTime createdAt;

    private Notification(NotificationId id, UUID userId, UUID customerId,
                         NotificationType type, String title, String message,
                         String referenceType, UUID referenceId,
                         boolean isRead, LocalDateTime readAt,
                         LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.customerId = customerId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.readAt = readAt;
        this.createdAt = createdAt;
    }

    public static Notification create(UUID userId, UUID customerId,
                                       NotificationType type, String title,
                                       String message, String referenceType, UUID referenceId) {
        if (userId == null && customerId == null) {
            throw new IllegalArgumentException("Either userId or customerId must be provided");
        }
        Objects.requireNonNull(type, "Notification type cannot be null");
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        return new Notification(NotificationId.generate(), userId, customerId,
                type, title, message, referenceType, referenceId,
                false, null, LocalDateTime.now(ZoneOffset.UTC));
    }

    public static Notification reconstitute(NotificationId id, UUID userId, UUID customerId,
                                             NotificationType type, String title,
                                             String message, String referenceType,
                                             UUID referenceId, boolean isRead,
                                             LocalDateTime readAt, LocalDateTime createdAt) {
        return new Notification(id, userId, customerId, type, title, message, referenceType,
                referenceId, isRead, readAt, createdAt);
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

    public UUID getCustomerId() {
        return customerId;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", type=" + type + ", title=" + title + "}";
    }
}
