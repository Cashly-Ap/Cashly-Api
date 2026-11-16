package com.cashly.cashly_api.platform.auth.authentication.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint Unit Tests")
class JwtAuthenticationEntryPointUnitTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @Mock
    private AuthenticationException authException;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @BeforeEach
    void setUp() throws IOException {
        when(response.getOutputStream()).thenReturn(outputStream);
        objectMapper.findAndRegisterModules();
    }

    @Test
    @DisplayName("should_SetUnauthorizedStatus_When_AuthenticationFails")
    void should_SetUnauthorizedStatus_When_AuthenticationFails()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        verify(response, times(1))
                .setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("should_SetJsonContentType_When_AuthenticationFails")
    void should_SetJsonContentType_When_AuthenticationFails()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        verify(response, times(1))
                .setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("should_WriteErrorResponse_When_AuthenticationFails")
    void should_WriteErrorResponse_When_AuthenticationFails()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        byte[] writtenBytes = captor.getValue();
        assertNotNull(writtenBytes);
        assertTrue(writtenBytes.length > 0);
    }

    @Test
    @DisplayName("should_IncludeTimestamp_When_WritingErrorResponse")
    void should_IncludeTimestamp_When_WritingErrorResponse()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        String jsonResponse = new String(captor.getValue());
        assertTrue(jsonResponse.contains("timestamp"));
    }

    @Test
    @DisplayName("should_IncludeStatusCode_When_WritingErrorResponse")
    void should_IncludeStatusCode_When_WritingErrorResponse()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        String jsonResponse = new String(captor.getValue());
        assertTrue(jsonResponse.contains("\"status\":401"));
    }

    @Test
    @DisplayName("should_IncludeErrorMessage_When_WritingErrorResponse")
    void should_IncludeErrorMessage_When_WritingErrorResponse()
            throws IOException {
        when(request.getHeader("Accept-Language")).thenReturn("en");
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        String jsonResponse = new String(captor.getValue());
        assertTrue(jsonResponse.contains("Unauthorized"));
        assertTrue(jsonResponse.contains("Authentication required"));
    }

    @Test
    @DisplayName("should_IncludeRequestPath_When_WritingErrorResponse")
    void should_IncludeRequestPath_When_WritingErrorResponse()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        String jsonResponse = new String(captor.getValue());
        assertTrue(jsonResponse.contains("/api/v1/protected"));
    }

    @Test
    @DisplayName("should_HandleDifferentPaths_When_AuthenticationFails")
    void should_HandleDifferentPaths_When_AuthenticationFails()
            throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/another-endpoint");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        String jsonResponse = new String(captor.getValue());
        assertTrue(jsonResponse.contains("/api/v1/another-endpoint"));
    }

    @Test
    @DisplayName("should_CallOutputStream_When_WritingResponse")
    void should_CallOutputStream_When_WritingResponse() throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        verify(response, times(1)).getOutputStream();
        verify(outputStream, times(1)).write(any(byte[].class));
    }

    @Test
    @DisplayName("should_ReturnValidJson_When_AuthenticationFails")
    void should_ReturnValidJson_When_AuthenticationFails() throws IOException {
        when(request.getServletPath()).thenReturn("/api/v1/protected");

        jwtAuthenticationEntryPoint.commence(request, response, authException);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream, times(1)).write(captor.capture());

        byte[] writtenBytes = captor.getValue();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        Map<String, Object> errorResponse = mapper.readValue(writtenBytes,
                Map.class);

        assertNotNull(errorResponse);
        assertTrue(errorResponse.containsKey("timestamp"));
        assertTrue(errorResponse.containsKey("status"));
        assertTrue(errorResponse.containsKey("error"));
        assertTrue(errorResponse.containsKey("message"));
        assertTrue(errorResponse.containsKey("path"));
    }
    @Test
    void defaultsToSpanishWhenNoLanguageHeaderIsAvailable() throws IOException {
        jwtAuthenticationEntryPoint.commence(request, response, authException);
        ArgumentCaptor<byte[]> payload = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream).write(payload.capture());
        String json = new String(payload.getValue(),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.contains("Debes iniciar sesión para continuar"));
        assertTrue(json.contains("AUTHENTICATION_REQUIRED"));
        verify(response).setHeader("Content-Language", "es");
    }

}
