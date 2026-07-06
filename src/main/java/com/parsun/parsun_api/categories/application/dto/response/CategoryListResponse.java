package com.parsun.parsun_api.categories.application.dto.response;

public record CategoryListResponse(
    String id,
    String name,
    String description,
    boolean active,
    boolean deletable
) {}
