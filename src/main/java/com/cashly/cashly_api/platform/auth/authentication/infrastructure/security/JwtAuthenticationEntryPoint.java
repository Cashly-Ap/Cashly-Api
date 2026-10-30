package com.cashly.cashly_api.platform.auth.authentication.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator translator = new com.cashly.cashly_api.shared.infrastructure.i18n.MessageTranslator();

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        var locale = java.util.Locale.forLanguageTag(com.cashly.cashly_api.shared.infrastructure.i18n.RequestLanguageFilter.requestedLanguage(request.getHeader("Accept-Language")));
        response.setHeader("Content-Language", locale.getLanguage());
        errorDetails.put("code", "AUTHENTICATION_REQUIRED");
        errorDetails.put("error", translator.translate("auth.unauthorized", locale));
        errorDetails.put("message", translator.translate("auth.authentication_required", locale));
        errorDetails.put("path", request.getServletPath());

        response.getOutputStream().write(objectMapper.writeValueAsBytes(errorDetails));
    }
}
