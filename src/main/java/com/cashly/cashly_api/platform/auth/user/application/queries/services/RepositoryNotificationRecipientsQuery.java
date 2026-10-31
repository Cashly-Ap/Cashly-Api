package com.cashly.cashly_api.platform.auth.user.application.queries.services;

import com.cashly.cashly_api.platform.auth.user.application.ports.in.NotificationRecipientsQuery;

import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import java.util.List;
import java.util.UUID;

public class RepositoryNotificationRecipientsQuery
        implements
            NotificationRecipientsQuery {
    private final UserRepository userRepository;

    public RepositoryNotificationRecipientsQuery(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UUID> findAllUserIds() {
        return userRepository.findAll().stream()
                .map(user -> user.getId().getValue()).toList();
    }
}
