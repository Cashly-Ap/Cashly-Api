package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;
import com.cashly.cashly_api.platform.notifications.infrastructure.messaging.NotificationTranslator;
import com.cashly.cashly_api.platform.notifications.infrastructure.messaging.WebSocketNotificationDeliveryAdapter;
import com.cashly.cashly_api.platform.notifications.infrastructure.persistence.mappers.NotificationJpaMapper;
import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
import com.cashly.cashly_api.platform.auth.preferences.domain.entity.UserPreferences;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.UUID;
import java.util.Locale;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationTranslationTest {
    private Notification sample(UUID user) {
        return Notification
                .create(user, NotificationType.INVOICE_CANCELLED,
                        "notifications.invoice_cancelled.title",
                        "notifications.invoice_cancelled.message", "INVOICE",
                        UUID.randomUUID())
                .withTranslation("notifications.invoice_cancelled.title",
                        "notifications.invoice_cancelled.message",
                        List.of("INV-123", "Customer reason"));
    }
    @Test
    void storedTemplateCanRenderInBothLanguagesAfterPreferenceChanges() {
        var source = sample(UUID.randomUUID());
        var stored = NotificationJpaMapper
                .toDomain(NotificationJpaMapper.toEntity(source));
        var renderer = new NotificationTranslator();
        assertThat(renderer.render(stored, Locale.ENGLISH).getMessage())
                .isEqualTo(
                        "Your invoice #INV-123 was cancelled: Customer reason");
        assertThat(renderer.render(stored, Locale.forLanguageTag("es"))
                .getMessage()).isEqualTo(
                        "Tu factura #INV-123 fue cancelada: Customer reason");
        assertThat(stored.getMessageKey())
                .isEqualTo("notifications.invoice_cancelled.message");
        assertThat(stored.getMessageArguments()).containsExactly("INV-123",
                "Customer reason");
    }
    @Test
    void websocketUsesRecipientsSavedLanguage() {
        var user = UUID.randomUUID();
        var preferences = mock(UserPreferencesRepository.class,
                CALLS_REAL_METHODS);
        when(preferences.findByUserId(user)).thenReturn(
                Optional.of(UserPreferences.defaults().withLanguage("en")));
        var messaging = mock(SimpMessagingTemplate.class);
        new WebSocketNotificationDeliveryAdapter(messaging, preferences)
                .deliver(sample(user));
        verify(messaging).convertAndSend(
                eq("/topic/notifications/user/" + user),
                argThat((Notification n) -> n.getTitle()
                        .equals("Invoice cancelled")));
    }
    @Test
    void legacyUserSuppliedNotificationsRemainUnchanged() {
        var raw = Notification.create(UUID.randomUUID(),
                NotificationType.ORDER_CREATED, "User title", "User text", null,
                null);
        assertThat(new NotificationTranslator().render(raw, Locale.ENGLISH))
                .isSameAs(raw);
    }
    @Test
    void everyNotificationTypeHasBothLanguagesAndKeepsArgumentData() {
        var translator = new NotificationTranslator();
        for (var type : NotificationType.values()) {
            if (type == NotificationType.ACTIVITY) {
                continue;
            }
            String base = "notifications."
                    + type.name().toLowerCase(Locale.ROOT);
            var notification = Notification
                    .create(UUID.randomUUID(), type, base + ".title",
                            base + ".message", "TEST", UUID.randomUUID())
                    .withTranslation(base + ".title", base + ".message",
                            List.of("Dato del usuario", "123.45"));
            var english = translator.render(notification, Locale.ENGLISH);
            var spanish = translator.render(notification,
                    Locale.forLanguageTag("es"));
            assertThat(english.getTitle()).isNotEqualTo(spanish.getTitle());
            assertThat(english.getMessage()).doesNotContain("notifications.",
                    "{0}", "{1}");
            assertThat(spanish.getMessage()).doesNotContain("notifications.",
                    "{0}", "{1}");
        }
    }

}
