package com.cashly.cashly_api.platform.notifications.infrastructure.config;

import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;



@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService tokenService;
    private final com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access;

    public WebSocketConfig(TokenService tokenService,
            com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService access) {
        this.tokenService = tokenService;
        this.access = access;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new NotificationChannelInterceptor(tokenService, access));
    }
}
