package com.cashly.cashly_api.platform.auth.token.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalAuthenticableSubjectGatewayTest {
    private final UserRepository users = mock(UserRepository.class);
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access = mock(
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService.class);
    private final LocalAuthenticableSubjectGateway gateway = new LocalAuthenticableSubjectGateway(
            users, access);

    @Test
    void should_resolve_user_without_querying_customers() {
        String id = UUID.randomUUID().toString();
        User user = mock(User.class);
        when(user.getId()).thenReturn(UserId.from(id));
        when(users.findById(UserId.from(id))).thenReturn(Optional.of(user));
        var subject = gateway.resolve(id, AuthenticableType.USER);
        assertEquals(id, subject.getSubjectId());
        assertEquals(AuthenticableType.USER, subject.getType());
    }

    @Test
    void should_preserve_user_not_found_error() {
        String id = UUID.randomUUID().toString();
        when(users.findById(UserId.from(id))).thenReturn(Optional.empty());
        assertEquals("User not found",
                assertThrows(UserNotFoundException.class,
                        () -> gateway.resolve(id, AuthenticableType.USER))
                        .getMessage());
    }
}
