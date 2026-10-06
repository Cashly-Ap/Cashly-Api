package com.cashly.cashly_api.portal.infrastructure.web;

import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.notifications.domain.entities.Notification;
import com.cashly.cashly_api.portal.application.services.PortalAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal/notifications")
public class PortalNotificationController {

    private final NotificationService notificationService;
    private final PortalAuthorizationService portalAuthorizationService;

    public PortalNotificationController(NotificationService notificationService,
                                          PortalAuthorizationService portalAuthorizationService) {
        this.notificationService = notificationService;
        this.portalAuthorizationService = portalAuthorizationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(@RequestParam(required = false) Boolean unread) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        List<NotificationResponse> notifications = notificationService.listByCustomer(customerId)
                .stream()
                .filter(n -> unread == null || n.isRead() != unread)
                .map(n -> new NotificationResponse(
                        n.getId().value(), n.getType().name(), n.getTitle(),
                        n.getMessage(), n.getReferenceType(), n.getReferenceId(),
                        n.isRead(), n.getCreatedAt(), n.getReadAt()))
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount() {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        long count = notificationService.countUnreadByCustomer(customerId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        var notification = notificationService.markAsReadByCustomer(id, customerId);
        return ResponseEntity.ok(new NotificationResponse(
                notification.getId().value(), notification.getType().name(),
                notification.getTitle(), notification.getMessage(),
                notification.getReferenceType(), notification.getReferenceId(),
                notification.isRead(), notification.getCreatedAt(),
                notification.getReadAt()));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        UUID customerId = portalAuthorizationService.getCurrentCustomerId();
        List<Notification> notifications = notificationService.listByCustomer(customerId);
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.markAsRead();
                notificationService.markAsReadByCustomer(n.getId().value(), customerId);
            }
        }
        return ResponseEntity.noContent().build();
    }

    public record NotificationResponse(UUID id, String type, String title, String message,
                                        String referenceType, UUID referenceId,
                                        boolean isRead, LocalDateTime createdAt,
                                        LocalDateTime readAt) {}

    public record UnreadCountResponse(long count) {}
}
