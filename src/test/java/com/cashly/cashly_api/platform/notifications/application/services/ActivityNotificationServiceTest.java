package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;
import com.cashly.cashly_api.platform.notifications.application.ports.out.ActivityActorGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationAudienceGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationDeliveryGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationRepository;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityNotificationServiceTest {

    @Mock
    private NotificationAudienceGateway audience;
    @Mock
    private ActivityActorGateway actors;
    @Mock
    private NotificationRepository notifications;
    @Mock
    private NotificationDeliveryGateway delivery;
    @Mock
    private CurrentActor currentActor;

    private ActivityNotificationService service;

    @BeforeEach
    void setUp() {
        service = new ActivityNotificationService(audience, actors,
                notifications, delivery, currentActor);
        lenient().when(notifications.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void read_only_actions_do_nothing() {
        service.record(ActivityEvent.of("FINANCE_BILLING_INVOICES", "VIEW_LIST",
                "INVOICE", UUID.randomUUID(), "INV-1", UUID.randomUUID()));
        verifyNoInteractions(audience, actors, notifications, delivery);
    }

    @Test
    void creates_one_notification_per_recipient_excluding_actor() {
        UUID actorId = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        when(actors.actorName(actorId)).thenReturn(Optional.of("Ana"));
        when(audience.findRecipients("FINANCE_BILLING_INVOICES", "EDIT",
                actorId)).thenReturn(List.of(recipient));

        service.record(ActivityEvent.of("FINANCE_BILLING_INVOICES", "EDIT",
                "INVOICE", UUID.randomUUID(), "FAC-001", actorId));

        ArgumentCaptor<Notification> captor = ArgumentCaptor
                .forClass(Notification.class);
        verify(notifications).save(captor.capture());
        var notification = captor.getValue();
        assertThat(notification.getUserId()).isEqualTo(recipient);
        assertThat(notification.getType())
                .isEqualTo(NotificationType.ACTIVITY);
        assertThat(notification.getTitleKey())
                .isEqualTo("notifications.activity.edit.title");
        assertThat(notification.getMessageArguments())
                .containsExactly("Ana",
                        "i18n:notifications.activity.entity.invoice.noun",
                        "FAC-001",
                        "i18n:notifications.activity.entity.invoice.label");
        verify(delivery).deliver(notification);
    }

    @Test
    void resolves_the_current_actor_when_the_event_does_not_provide_one() {
        UUID actorId = UUID.randomUUID();
        when(currentActor.requireId()).thenReturn(actorId);
        when(actors.actorName(actorId)).thenReturn(Optional.of("Ana"));
        when(audience.findRecipients(any(), any(), any()))
                .thenReturn(List.of());

        service.record(ActivityEvent.of("CATALOG_PRODUCTS", "CREATE",
                "PRODUCT", UUID.randomUUID(), "P-1", null));

        verify(audience).findRecipients("CATALOG_PRODUCTS", "CREATE", actorId);
        verifyNoInteractions(notifications, delivery);
    }
}
