package com.cashly.cashly_api.categories.application.dto.response;

import com.cashly.cashly_api.categories.domain.Category;

public record CategorySummary(Category category, long productCount) { }
