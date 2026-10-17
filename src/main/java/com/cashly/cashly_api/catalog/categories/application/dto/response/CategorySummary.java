package com.cashly.cashly_api.catalog.categories.application.dto.response;

import com.cashly.cashly_api.catalog.categories.domain.entity.Category;

public record CategorySummary(Category category, long productCount) { }
