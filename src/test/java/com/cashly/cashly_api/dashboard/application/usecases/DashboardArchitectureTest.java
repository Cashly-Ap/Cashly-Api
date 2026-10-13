package com.cashly.cashly_api.dashboard.application.usecases;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardArchitectureTest {

    @Test
    void should_depend_on_application_ports_instead_of_jdbc() {
        assertThat(Arrays.stream(GetDashboardUseCase.class.getDeclaredFields())
                .map(Field::getType)
                .map(Class::getPackageName))
                .allMatch(packageName -> packageName.startsWith(
                        "com.cashly.cashly_api.dashboard.application.ports.out"));
    }
}
