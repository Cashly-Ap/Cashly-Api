package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class PermissionWebConfig implements WebMvcConfigurer  {
    private final PermissionCoverageInterceptor interceptor;
    public PermissionWebConfig(PermissionCoverageInterceptor interceptor)  {
        this.interceptor=interceptor;
    }
    @Override public void addInterceptors(InterceptorRegistry registry)  {
        registry.addInterceptor(interceptor).addPathPatterns("/api/**");
    }
}
