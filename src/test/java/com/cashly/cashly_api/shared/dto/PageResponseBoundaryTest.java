package com.cashly.cashly_api.shared.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseBoundaryTest {
    @Test
    void should_not_expose_framework_types_to_domain_repositories() {
        assertThat(Arrays.stream(PageResponse.class.getDeclaredMethods())
                .flatMap(method -> Arrays.stream(method.getParameterTypes()))
                .map(Class::getPackageName))
                .noneMatch(packageName -> packageName.startsWith("org.springframework"));
    }
}
