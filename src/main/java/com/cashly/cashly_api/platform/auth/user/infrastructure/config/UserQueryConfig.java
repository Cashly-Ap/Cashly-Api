package com.cashly.cashly_api.platform.auth.user.infrastructure.config;

import com.cashly.cashly_api.platform.auth.user.application.ports.in.NotificationRecipientsQuery;
import com.cashly.cashly_api.platform.auth.user.application.queries.services.RepositoryNotificationRecipientsQuery;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserQueryConfig {
    @Bean
    public NotificationRecipientsQuery notificationRecipientsQuery(
            UserRepository userRepository) {
        return new RepositoryNotificationRecipientsQuery(userRepository);
    }
}
