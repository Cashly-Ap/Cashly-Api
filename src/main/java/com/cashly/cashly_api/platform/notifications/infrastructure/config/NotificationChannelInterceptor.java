package com.cashly.cashly_api.platform.notifications.infrastructure.config;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

/** Notifications are self-service; STOMP never grants business-operation privileges. */
public class NotificationChannelInterceptor implements ChannelInterceptor {
    private final TokenService tokens;
    private final AccessService access;

    public NotificationChannelInterceptor(TokenService tokens, AccessService access) {
        this.tokens = tokens;
        this.access = access;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null || headers.getCommand() == null) {
            return message;
        }
        var command = headers.getCommand();
        if (command == StompCommand.DISCONNECT || command == StompCommand.UNSUBSCRIBE) {
            return message;
        }
        if (command == StompCommand.CONNECT) {
            String authorization = headers.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AccessDeniedException("Authentication required");
            }
            String token = authorization.substring(7);
            if (!tokens.validateToken(token)) {
                throw new AccessDeniedException("Invalid token");
            }
            String subject = tokens.extractSubjectId(token);
            access.resolve(UUID.fromString(subject));
            headers.setUser(new UsernamePasswordAuthenticationToken(subject, null, List.of()));
            return message;
        }
        if (headers.getUser() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        String subject = headers.getUser().getName();
        access.resolve(UUID.fromString(subject));
        if (command == StompCommand.SUBSCRIBE
                && !String.format("/topic/notifications/user/%s", subject).equals(headers.getDestination())) {
            throw new AccessDeniedException("Only your own notifications can be subscribed");
        }
        if (command == StompCommand.SEND) {
            throw new AccessDeniedException("Notifications do not accept client operations");
        }
        return message;
    }
}
