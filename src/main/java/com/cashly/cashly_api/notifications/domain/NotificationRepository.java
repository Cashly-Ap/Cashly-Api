package com.cashly.cashly_api.notifications.domain;

import com.cashly.cashly_api.notifications.domain.Notification;
import com.cashly.cashly_api.notifications.domain.valueobjects.NotificationId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long countUnreadByUserId(UUID userId);
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    long countUnreadByCustomerId(UUID customerId);
    Optional<Notification> findById(NotificationId id);
    void markAllAsReadByUserId(UUID userId);
    void deleteById(NotificationId id);
}
