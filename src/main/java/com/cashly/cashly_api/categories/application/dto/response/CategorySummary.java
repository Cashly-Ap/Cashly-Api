package com.cashly.cashly_api.categories.application.dto.response;

import com.cashly.cashly_api.categories.domain.entity.Category;

public record CategorySummary(Category category, long productCount) { }
