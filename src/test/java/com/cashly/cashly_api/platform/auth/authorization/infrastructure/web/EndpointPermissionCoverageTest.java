package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.cashly.cashly_api.platform.auth.mfa.infrastructure.web.MfaController;
class EndpointPermissionCoverageTest {
    private static final Set<String> SELF_SERVICE = Set.of(
            "AuthController#login", "AuthController#refresh",
            "AuthController#logout", "AuthController#changePassword",
            "AuthController#profile", "AuthController#updateProfile",
            "CurrentAccessController#current", "NotificationController#list",
            "NotificationController#unreadCount",
            "NotificationController#markAsRead",
            "NotificationController#markAllAsRead");

    @Test
    void everyControllerOperationHasExplicitPermissionsOrASelfServiceContract()
            throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(
                new AnnotationTypeFilter(RestController.class));
        var controllers = scanner
                .findCandidateComponents("com.cashly.cashly_api");
        var missing = new ArrayList<String>();
        var legacy = new ArrayList<String>();
        int mappings = 0;
        for (var definition : controllers) {
            var controller = Class.forName(definition.getBeanClassName());
            for (var method : controller.getDeclaredMethods()) {
                if (!AnnotatedElementUtils.hasAnnotation(method,
                        RequestMapping.class)) {
                    continue;
                }
                mappings++;
                var permission = AnnotatedElementUtils
                        .findMergedAnnotation(method, PreAuthorize.class);
                if (permission == null) {
                    permission = AnnotatedElementUtils.findMergedAnnotation(
                            controller, PreAuthorize.class);
                }
                String operation = controller.getSimpleName() + "#"
                        + method.getName();
                if (permission == null && !SELF_SERVICE.contains(operation)) {
                    missing.add(operation);
                }
                if (permission != null
                        && (permission.value().contains("hasRole(")
                                || permission.value().contains("hasAnyRole(")
                                || permission.value().contains("MODULE_"))) {
                    legacy.add(operation);
                }
            }
        }
        assertThat(mappings)
                .as("The controller scan must discover the real API")
                .isGreaterThan(180);
        assertThat(missing)
                .as("Protected operations require explicit permissions")
                .isEmpty();
        assertThat(legacy).as(
                "Fixed role names and direct user grants cannot authorize operations")
                .isEmpty();
    }

    @Test
    void ownProfileHasAnExplicitSelfServiceException() throws Exception {
        var controller = com.cashly.cashly_api.platform.auth.authentication.infrastructure.web.AuthController.class;
        var handler = new HandlerMethod(mock(controller),
                controller.getDeclaredMethod("profile",
                        org.springframework.security.core.Authentication.class));
        assertThat(new PermissionCoverageInterceptor().preHandle(
                mock(jakarta.servlet.http.HttpServletRequest.class),
                mock(jakarta.servlet.http.HttpServletResponse.class), handler))
                .isTrue();
    }

    @Test
    void mfaOperationsHaveExplicitSelfServiceAndPublicVerificationContracts() {
        var controller = com.cashly.cashly_api.platform.auth.mfa.infrastructure.web.MfaController.class;
        var interceptor = new PermissionCoverageInterceptor();
        int mappings = 0;
        for (var method : controller.getDeclaredMethods()) {
            if (!AnnotatedElementUtils.hasAnnotation(method,
                    RequestMapping.class)) {
                continue;
            }
            mappings++;
            var permission = method.getAnnotation(PreAuthorize.class);
            assertThat(permission).isNotNull();
            assertThat(permission.value())
                    .isEqualTo(method.getName().equals("verify")
                            ? "permitAll()"
                            : "isAuthenticated()");
            var handler = new HandlerMethod(mock(controller), method);
            assertThat(interceptor.preHandle(
                    mock(jakarta.servlet.http.HttpServletRequest.class),
                    mock(jakarta.servlet.http.HttpServletResponse.class),
                    handler)).isTrue();
        }
        assertThat(mappings).isEqualTo(5);
    }

    @Test
    void newUnmappedOperationsFailClosed() throws Exception {
        var handler = new HandlerMethod(new UnmappedController(),
                UnmappedController.class.getDeclaredMethod("write"));
        var interceptor = new PermissionCoverageInterceptor();

        assertThatThrownBy(() -> interceptor.preHandle(
                mock(jakarta.servlet.http.HttpServletRequest.class),
                mock(jakarta.servlet.http.HttpServletResponse.class), handler))
                .isInstanceOf(AccessDeniedException.class);
    }

    static class UnmappedController {
        public void write() {
        }
    }
}
