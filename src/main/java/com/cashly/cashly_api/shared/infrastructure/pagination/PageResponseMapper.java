package com.cashly.cashly_api.shared.infrastructure.pagination;

import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class PageResponseMapper {
    private PageResponseMapper() {}

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(),
                page.getSize(), page.getNumber(), page.isFirst(), page.isLast(), page.isEmpty());
    }
}
