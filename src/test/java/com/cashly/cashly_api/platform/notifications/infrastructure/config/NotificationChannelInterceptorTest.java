package com.cashly.cashly_api.platform.notifications.infrastructure.config;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationChannelInterceptorTest {
    private final TokenService tokens = mock(TokenService.class);
    private final AccessService access = mock(AccessService.class);
    private final NotificationChannelInterceptor interceptor = new NotificationChannelInterceptor(
            tokens, access);
    private final UUID userId = UUID.randomUUID();

    private void send(StompHeaderAccessor headers) {
        headers.setLeaveMutable(true);
        interceptor.preSend(MessageBuilder.createMessage(new byte[0],
                headers.getMessageHeaders()), null);
    }

    @Test
    void connect_rejects_missing_token_or_deactivated_account_and_role() {
        assertThatThrownBy(
                () -> send(StompHeaderAccessor.create(StompCommand.CONNECT)))
                .isInstanceOf(AccessDeniedException.class);
        when(tokens.validateToken("token")).thenReturn(true);
        when(tokens.extractSubjectId("token")).thenReturn(userId.toString());
        when(access.resolve(userId))
                .thenThrow(new AccessDeniedException("Inactive"));
        var headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "Bearer token");
        assertThatThrownBy(() -> send(headers))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void subscriptions_require_current_access_and_the_actors_own_destination() {
        var headers = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headers.setUser(new UsernamePasswordAuthenticationToken(
                userId.toString(), null, List.of()));
        headers.setDestination(
                "/topic/notifications/user/" + UUID.randomUUID());
        assertThatThrownBy(() -> send(headers))
                .isInstanceOf(AccessDeniedException.class);
        var own = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        own.setUser(new UsernamePasswordAuthenticationToken(userId.toString(),
                null, List.of()));
        own.setDestination("/topic/notifications/user/" + userId);
        send(own);
        when(access.resolve(userId))
                .thenThrow(new AccessDeniedException("Revoked"));
        assertThatThrownBy(() -> send(own))
                .isInstanceOf(AccessDeniedException.class);
    }
}
