package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationDeliveryGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationRepository;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationId;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryGateway deliveryGateway;

    public NotificationService(NotificationRepository notificationRepository,
            NotificationDeliveryGateway deliveryGateway) {
        this.notificationRepository = notificationRepository;
        this.deliveryGateway = deliveryGateway;
    }

    @Transactional
    public Notification create(UUID userId, NotificationType type, String title,
            String message, String referenceType, UUID referenceId) {
        Notification notification = Notification.create(userId, type, title,
                message, referenceType, referenceId);
        Notification saved = notificationRepository.save(notification);
        deliveryGateway.deliver(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> listByUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public Notification markAsRead(UUID id, UUID userId) {
        Notification notification = notificationRepository
                .findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + id));
        if (!userId.equals(notification.getUserId())) {
            throw new IllegalArgumentException(
                    "Notification does not belong to the current user");
        }
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Notification notification = notificationRepository
                .findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + id));
        if (!userId.equals(notification.getUserId())) {
            throw new IllegalArgumentException(
                    "Notification does not belong to the current user");
        }
        notificationRepository.deleteById(notification.getId());
    }
}
