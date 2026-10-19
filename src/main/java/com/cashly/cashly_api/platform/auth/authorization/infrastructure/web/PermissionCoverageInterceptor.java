package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionCoverageInterceptor implements HandlerInterceptor  {
    private static final Set<String> SELF_SERVICE = Set.of(
    "AuthController#login", "AuthController#refresh", "AuthController#logout", "AuthController#changePassword", "AuthController#profile", "AuthController#updateProfile",
    "CurrentAccessController#current",
    "NotificationController#list", "NotificationController#unreadCount", "NotificationController#markAsRead", "NotificationController#markAllAsRead");
    @Override
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)  {
        if (!(handler instanceof HandlerMethod method)) return true;
        if (!method.getBeanType().getPackageName().startsWith("com.cashly.cashly_api")) return true;
        if (method.hasMethodAnnotation(PreAuthorize.class) || method.getBeanType().isAnnotationPresent(PreAuthorize.class)) return true;
        String key=method.getBeanType().getSimpleName()+"#"+method.getMethod().getName();
        if (SELF_SERVICE.contains(key)) return true;
        throw new AccessDeniedException("Endpoint has no permission mapping");
    }
}
