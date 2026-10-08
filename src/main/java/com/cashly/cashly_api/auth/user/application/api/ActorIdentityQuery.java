package com.cashly.cashly_api.auth.user.application.api;
import java.util.Optional;
import java.util.UUID;
public interface ActorIdentityQuery {
    Optional<Actor> findById(UUID id);
    boolean matchesPassword(UUID id, String password);
    record Actor(UUID id, String name) {}
}
