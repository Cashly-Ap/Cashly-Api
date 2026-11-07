package com.cashly.cashly_api.platform.notifications.application.ports.in;


import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ActivityEvent(String module, String action,
        String referenceType, UUID referenceId, String referenceLabel,
        UUID actorId, String entityLabelKey, List<String> extraArguments) {

    public ActivityEvent {
        Objects.requireNonNull(module, "module is required");
        Objects.requireNonNull(action, "action is required");
        Objects.requireNonNull(referenceType, "referenceType is required");
        Objects.requireNonNull(referenceId, "referenceId is required");
        Objects.requireNonNull(referenceLabel, "referenceLabel is required");
        extraArguments = extraArguments == null
                ? List.of()
                : List.copyOf(extraArguments);
    }

    public static ActivityEvent of(String module, String action,
            String referenceType, UUID referenceId, String referenceLabel,
            UUID actorId) {
        return new ActivityEvent(module, action, referenceType, referenceId,
                referenceLabel, actorId, null, List.of());
    }
}
