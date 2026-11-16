package com.cashly.cashly_api.shared.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponsesTest {
    @Test
    void should_filter_before_paging() {
        var result = PageResponses.filterAndPage(List.of("blocked", "one", "blocked", "two"),
                value -> !value.equals("blocked"), 1, 1);

        assertThat(result.content()).containsExactly("two");
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(2);
    }
}
