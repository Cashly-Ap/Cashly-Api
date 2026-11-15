package com.cashly.cashly_api.shared.dto;

import java.util.List;
import java.util.function.Predicate;

public final class PageResponses {
    private PageResponses() {
    }

    public static <T> PageResponse<T> filterAndPage(List<T> values,
            Predicate<T> predicate, int page, int size) {
        return page(values.stream().filter(predicate).toList(), page, size);
    }

    public static <T> PageResponse<T> page(List<T> values, int page,
            int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }
        int from = Math.min(page * size, values.size());
        int to = Math.min(from + size, values.size());
        List<T> content = values.subList(from, to);
        int totalPages = (int) Math.ceil((double) values.size() / size);
        return new PageResponse<>(content, values.size(), totalPages, size,
                page, page == 0, page >= totalPages - 1, content.isEmpty());
    }
}
