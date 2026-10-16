package com.cashly.cashly_api.categories.application.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @Size(max = 300, message = "Description cannot exceed 300 characters")
    String description,

    Boolean active
) {}
