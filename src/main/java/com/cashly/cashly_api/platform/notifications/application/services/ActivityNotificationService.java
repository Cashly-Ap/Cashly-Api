package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.notifications.application.activity.services.ActivityActionPolicy;
import com.cashly.cashly_api.platform.notifications.application.activity.services.ActivityEntityCatalog;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;
import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;
import com.cashly.cashly_api.platform.notifications.application.ports.out.ActivityActorGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationAudienceGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationDeliveryGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationRepository;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityNotificationService implements ActivityNotificationRecorder {

    private final NotificationAudienceGateway audience;
    private final ActivityActorGateway actors;
    private final NotificationRepository notifications;
    private final NotificationDeliveryGateway delivery;
    private final CurrentActor currentActor;

    public ActivityNotificationService(NotificationAudienceGateway audience,
            ActivityActorGateway actors, NotificationRepository notifications,
            NotificationDeliveryGateway delivery, CurrentActor currentActor) {
        this.audience = audience;
        this.actors = actors;
        this.notifications = notifications;
        this.delivery = delivery;
        this.currentActor = currentActor;
    }

    @Override
    @Transactional
    public void record(ActivityEvent event) {
        if (event == null
                || !ActivityActionPolicy.isMutating(event.action())) {
            return;
        }
        UUID actorId = resolveActorId(event.actorId());
        if (actorId == null) {
            return;
        }
        String actorName = actors.actorName(actorId).orElse(null);
        if (actorName == null) {
            return;
        }
        List<UUID> recipients = audience.findRecipients(event.module(),
                event.action(), actorId);
        if (recipients.isEmpty()) {
            return;
        }
        String titleKey = ActivityActionPolicy.titleKey(event.action());
        String messageKey = ActivityActionPolicy.messageKey(event.action());
        String entityKey = event.entityLabelKey() != null
                ? event.entityLabelKey()
                : ActivityEntityCatalog.labelKey(event.referenceType());
        String entityDisplayKey = ActivityEntityCatalog
                .displayLabelKey(event.referenceType());
        List<String> arguments = new ArrayList<>();
        arguments.add(actorName);
        arguments.add("i18n:" + entityKey);
        arguments.add(event.referenceLabel());
        arguments.add("i18n:" + entityDisplayKey);
        arguments.addAll(event.extraArguments());
        for (UUID recipient : recipients) {
            Notification notification = Notification.create(recipient,
                    NotificationType.ACTIVITY, titleKey, messageKey,
                    event.referenceType(), event.referenceId())
                    .withTranslation(titleKey, messageKey, arguments);
            Notification saved = notifications.save(notification);
            delivery.deliver(saved);
        }
    }

    private UUID resolveActorId(UUID actorId) {
        if (actorId != null) {
            return actorId;
        }
        try {
            return currentActor.requireId();
        } catch (RuntimeException unavailable) {
            return null;
        }
    }
}
