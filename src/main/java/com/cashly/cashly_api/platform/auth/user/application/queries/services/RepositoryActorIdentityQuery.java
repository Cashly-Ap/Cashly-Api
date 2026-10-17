package com.cashly.cashly_api.platform.auth.user.application.queries.services;
import com.cashly.cashly_api.platform.auth.user.application.ports.in.ActorIdentityQuery;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import java.util.Optional;
import java.util.UUID;
public class RepositoryActorIdentityQuery implements ActorIdentityQuery {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    public RepositoryActorIdentityQuery(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }
    public Optional<Actor> findById(UUID id) {
        return users.findById(new UserId(id)).map(user -> new Actor(id, user.getUserName().getValue()));
    }
    public boolean matchesPassword(UUID id, String password) {
        var user = users.findById(new UserId(id)).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return passwords.matches(password, user.getPassword().getHashedValue());
    }
}
