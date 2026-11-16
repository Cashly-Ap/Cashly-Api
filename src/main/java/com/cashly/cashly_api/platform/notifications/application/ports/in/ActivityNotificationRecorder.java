package com.cashly.cashly_api.platform.notifications.application.ports.in;


public interface ActivityNotificationRecorder {

    ActivityNotificationRecorder NONE = event -> {
    };

    void record(ActivityEvent event);
}
