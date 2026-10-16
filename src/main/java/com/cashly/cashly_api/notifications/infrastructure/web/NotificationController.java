package com.cashly.cashly_api.notifications.infrastructure.web;

import com.cashly.cashly_api.notifications.application.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) Boolean unread) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        List<NotificationResponse> notifications = notificationService.listByUser(userId)
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
    public ResponseEntity<UnreadCountResponse> unreadCount(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        var notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(new NotificationResponse(
                notification.getId().value(), notification.getType().name(),
                notification.getTitle(), notification.getMessage(),
                notification.getReferenceType(), notification.getReferenceId(),
                notification.isRead(), notification.getCreatedAt(),
                notification.getReadAt()));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    public record NotificationResponse(UUID id, String type, String title, String message,
                                        String referenceType, UUID referenceId,
                                        boolean isRead, LocalDateTime createdAt,
                                        LocalDateTime readAt) {}

    public record UnreadCountResponse(long count) {}
}
