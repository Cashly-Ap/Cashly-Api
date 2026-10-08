package com.cashly.cashly_api.auth.user.application.queries;

import com.cashly.cashly_api.auth.user.application.api.NotificationRecipientsQuery;

import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import java.util.List;
import java.util.UUID;

public class RepositoryNotificationRecipientsQuery implements NotificationRecipientsQuery {
    private final UserRepository userRepository;

    public RepositoryNotificationRecipientsQuery(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UUID> findAllUserIds() {
        return userRepository.findAll().stream().map(user -> user.getId().getValue()).toList();
    }
}
