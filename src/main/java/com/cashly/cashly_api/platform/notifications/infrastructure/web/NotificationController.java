package com.cashly.cashly_api.platform.notifications.infrastructure.web;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.notifications.application.services.NotificationPreferencesService;
import com.cashly.cashly_api.platform.notifications.application.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferencesService preferencesService;
    private final com.cashly.cashly_api.platform.notifications.infrastructure.messaging.NotificationTranslator translator = new com.cashly.cashly_api.platform.notifications.infrastructure.messaging.NotificationTranslator();

    public NotificationController(NotificationService notificationService,
            NotificationPreferencesService preferencesService) {
        this.notificationService = notificationService;
        this.preferencesService = preferencesService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) Boolean unread) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        List<NotificationResponse> notifications = notificationService
                .listByUser(userId).stream()
                .filter(n -> unread == null || n.isRead() != unread)
                .map(n -> translator.render(n,
                        org.springframework.context.i18n.LocaleContextHolder
                                .getLocale()))
                .map(n -> new NotificationResponse(n.getId().value(),
                        n.getType().name(), n.getTitle(), n.getMessage(),
                        n.getReferenceType(), n.getReferenceId(), n.isRead(),
                        n.getCreatedAt(), n.getReadAt()))
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        var notification = translator.render(
                notificationService.markAsRead(id, userId),
                org.springframework.context.i18n.LocaleContextHolder
                        .getLocale());
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

    @GetMapping("/preferences")
    public ResponseEntity<PreferencesResponse> preferences(
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return ResponseEntity.ok(toResponse(preferencesService.get(userId)));
    }

    @PutMapping("/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(
            Authentication authentication,
            @RequestBody UpdatePreferencesRequest request) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        Map<String, Boolean> modules = new java.util.HashMap<>();
        if (request.modules() != null) {
            for (ModulePreferenceRequest module : request.modules()) {
                modules.put(parseModule(module.module()), module.enabled());
            }
        }
        return ResponseEntity.ok(toResponse(preferencesService.update(userId,
                request.enabled(), modules)));
    }

    private String parseModule(String module) {
        if (module == null) {
            throw new IllegalArgumentException("Module is required");
        }
        try {
            PermissionModule.valueOf(module);
            return module;
        } catch (IllegalArgumentException unknown) {
            throw new IllegalArgumentException("Unknown module: " + module);
        }
    }

    private PreferencesResponse toResponse(
            NotificationPreferencesService.PreferencesView view) {
        return new PreferencesResponse(view.enabled(), view.modules().stream()
                .map(module -> new ModulePreferenceResponse(
                        module.module(), module.labelKey(),
                        module.enabled()))
                .toList());
    }

    public record NotificationResponse(UUID id, String type, String title,
            String message, String referenceType, UUID referenceId,
            boolean isRead, LocalDateTime createdAt, LocalDateTime readAt) {
    }

    public record UnreadCountResponse(long count) {
    }

    public record PreferencesResponse(boolean enabled,
            List<ModulePreferenceResponse> modules) {
    }

    public record ModulePreferenceResponse(String module, String labelKey,
            boolean enabled) {
    }

    public record UpdatePreferencesRequest(boolean enabled,
            List<ModulePreferenceRequest> modules) {
    }

    public record ModulePreferenceRequest(String module, boolean enabled) {
    }
}
