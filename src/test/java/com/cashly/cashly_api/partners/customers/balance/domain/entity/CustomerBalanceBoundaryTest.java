package com.cashly.cashly_api.partners.customers.balance.domain.entity;

import com.cashly.cashly_api.partners.customers.balance.domain.repository.CustomerBalanceRepository;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerBalanceBoundaryTest {
    @Test
    void should_expose_neutral_pagination_to_the_domain() {
        assertThat(Arrays
                .stream(CustomerBalanceRepository.class.getDeclaredMethods())
                .flatMap(method -> Stream.concat(
                        Stream.of(method.getReturnType()),
                        Arrays.stream(method.getParameterTypes())))
                .map(Class::getPackageName))
                .noneMatch(packageName -> packageName
                        .startsWith("org.springframework"));
    }
}
