package com.cashly.cashly_api.categories.application.dto.response;

import com.cashly.cashly_api.categories.domain.entities.Category;

public record CategorySummary(Category category, long productCount) { }
