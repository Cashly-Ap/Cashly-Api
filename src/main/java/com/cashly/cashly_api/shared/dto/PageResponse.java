package com.cashly.cashly_api.shared.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int size,
    int number,
    boolean first,
    boolean last,
    boolean empty
) {
}
