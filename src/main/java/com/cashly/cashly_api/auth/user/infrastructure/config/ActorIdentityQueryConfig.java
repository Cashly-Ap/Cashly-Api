package com.cashly.cashly_api.auth.user.infrastructure.config;
import com.cashly.cashly_api.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.auth.user.application.queries.services.RepositoryActorIdentityQuery;
import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class ActorIdentityQueryConfig {
    @Bean
    public ActorIdentityQuery actorIdentityQuery(UserRepository users, PasswordEncoder passwords) {
        return new RepositoryActorIdentityQuery(users, passwords);
    }
}
