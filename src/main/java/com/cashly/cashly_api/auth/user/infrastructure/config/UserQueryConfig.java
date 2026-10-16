package com.cashly.cashly_api.auth.user.infrastructure.config;

import com.cashly.cashly_api.auth.user.application.ports.in.NotificationRecipientsQuery;
import com.cashly.cashly_api.auth.user.application.queries.RepositoryNotificationRecipientsQuery;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserQueryConfig {
    @Bean
    public NotificationRecipientsQuery notificationRecipientsQuery(UserRepository userRepository) {
        return new RepositoryNotificationRecipientsQuery(userRepository);
    }
}
