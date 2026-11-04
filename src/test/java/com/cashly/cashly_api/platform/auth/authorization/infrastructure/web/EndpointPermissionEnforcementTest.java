package com.cashly.cashly_api.platform.auth.authorization.infrastructure.web;

import com.cashly.cashly_api.platform.auth.authorization.domain.services.PermissionRegistry;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EndpointPermissionEnforcementTest {
    private static final Pattern AUTHORITY = Pattern
            .compile("hasAuthority\\('([^']+)'\\)");
    private static final Set<String> ALL = PermissionRegistry.modules()
            .entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                    .map(action -> entry.getKey().name() + "_" + action.name()))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    record Operation(Method method, String policy) {
        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName() + "#"
                    + method.getName();
        }
    }

    static Stream<Operation> operations() throws ClassNotFoundException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(
                new AnnotationTypeFilter(RestController.class));
        var result = new ArrayList<Operation>();
        for (var definition : scanner
                .findCandidateComponents("com.cashly.cashly_api")) {
            var controller = Class.forName(definition.getBeanClassName());
            for (var method : controller.getDeclaredMethods()) {
                if (!AnnotatedElementUtils.hasAnnotation(method,
                        RequestMapping.class))
                    continue;
                var annotation = AnnotatedElementUtils
                        .findMergedAnnotation(method, PreAuthorize.class);
                if (annotation == null) {
                    annotation = AnnotatedElementUtils.findMergedAnnotation(
                            controller, PreAuthorize.class);
                }
                if (annotation != null)
                    result.add(new Operation(method, annotation.value()));
            }
        }
        assertThat(result.size()).isGreaterThan(170);
        return result.stream();
    }

    @Test
    void everyProtectedEndpointDeclaresExactlyOnePermission() throws Exception {
        operations().forEach(operation -> {
            if (operation.policy().equals("isAuthenticated()")
                    || operation.policy().equals("permitAll()")) {
                return;
            }
            assertThat(AUTHORITY.matcher(operation.policy()).results().count())
                    .as("%s must declare exactly one permission", operation)
                    .isEqualTo(1);
        });
    }

    @ParameterizedTest(name = "{0}: no module access")
    @MethodSource("operations")
    void deniesAnAuthenticatedUserWithoutPermissions(Operation operation) {
        assertThat(authorized(operation, Set.of()))
                .isEqualTo(operation.policy().equals("isAuthenticated()")
                        || operation.policy().equals("permitAll()"));
    }

    @ParameterizedTest(name = "{0}: read only")
    @MethodSource("operations")
    void readOnlyNeverAuthorizesWritesOrExports(Operation operation) {
        var read = ALL.stream().filter(authority -> authority.endsWith(":READ"))
                .collect(java.util.stream.Collectors.toSet());
        boolean requiresOnlyRead = required(operation).stream()
                .allMatch(value -> value.endsWith(":READ"));
        assertThat(authorized(operation, read)).isEqualTo(requiresOnlyRead);
    }

    @ParameterizedTest(name = "{0}: required actions")
    @MethodSource("operations")
    void requiresEveryDeclaredActionAndAcceptsAuthorizedUsers(
            Operation operation) {
        assertThat(authorized(operation, ALL)).as("%s", operation).isTrue();
        if (operation.policy().contains(" or ")) {
            assertThat(
                    authorized(operation, Set.of("USERS_VIEW_LIST", "USERS_CREATE")))
                    .isTrue();
            assertThat(
                    authorized(operation, Set.of("USERS_VIEW_LIST", "USERS_EDIT")))
                    .isTrue();
            assertThat(authorized(operation,
                    Set.of("USERS_CREATE", "USERS_EDIT"))).isFalse();
            return;
        }
        for (var authority : required(operation)) {
            var reduced = new HashSet<>(ALL);
            reduced.remove(authority);
            assertThat(authorized(operation, reduced))
                    .as("Missing %s", authority).isFalse();
        }
    }

    private static List<String> required(Operation operation) {
        return AUTHORITY.matcher(operation.policy()).results()
                .map(match -> match.group(1)).toList();
    }

    private boolean authorized(Operation operation, Set<String> authorities) {
        var invocation = mock(MethodInvocation.class);
        when(invocation.getMethod()).thenReturn(operation.method());
        when(invocation.getThis())
                .thenReturn(mock(operation.method().getDeclaringClass()));
        when(invocation.getArguments())
                .thenReturn(new Object[operation.method().getParameterCount()]);
        var authentication = new UsernamePasswordAuthenticationToken(
                "current-user", null,
                authorities.stream().map(SimpleGrantedAuthority::new).toList());
        var decision = new PreAuthorizeAuthorizationManager()
                .check(() -> authentication, invocation);
        assertThat(decision).as("A concrete authorization policy is required")
                .isNotNull();
        return decision.isGranted();
    }
}
