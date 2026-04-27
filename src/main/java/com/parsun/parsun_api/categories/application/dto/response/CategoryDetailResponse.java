package com.parsun.parsun_api.categories.application.dto.response;

import java.time.LocalDateTime;

public record CategoryDetailResponse(
    String id,
    String name,
    String description,
    boolean active,
    long productCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
