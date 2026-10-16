package com.cashly.cashly_api.categories.domain.entity;

import com.cashly.cashly_api.categories.domain.repository.CategoryRepository;

import com.cashly.cashly_api.price_types.domain.repository.PriceTypeRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CatalogUsageBoundaryTest {
    @Test
    void shouldKeepProductUsageOutsideCatalogDomainRepositories() {
        for (Class<?> repository : new Class<?>[]{CategoryRepository.class, PriceTypeRepository.class}) {
            assertFalse(Arrays.stream(repository.getDeclaredMethods())
                    .anyMatch(method -> method.getName().contains("Products")));
        }
    }
}
