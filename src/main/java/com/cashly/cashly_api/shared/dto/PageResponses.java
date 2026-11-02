package com.cashly.cashly_api.shared.dto;

import java.util.List;
import java.util.function.Predicate;

public final class PageResponses {
    private PageResponses() {
    }

    public static <T> PageResponse<T> filterAndPage(List<T> values,
            Predicate<T> predicate, int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }
        List<T> filtered = values.stream().filter(predicate).toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = (int) Math.ceil((double) filtered.size() / size);
        return new PageResponse<>(filtered.subList(from, to), filtered.size(),
                totalPages, size, page, page == 0, totalPages == 0 || page >= totalPages - 1,
                filtered.isEmpty());
    }
}
