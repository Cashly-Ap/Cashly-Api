package com.cashly.cashly_api.products.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateProductRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    String name,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @DecimalMin(value = "0.0", message = "Purchase price must be non-negative")
    BigDecimal purchasePrice,

    Boolean composite,

    @Valid
    List<ComponentRequest> components,

    @Valid
    List<ProductPriceRequest> productPrices,

    Boolean active
) {
    public record ComponentRequest(
        @NotNull(message = "Component product ID is required")
        UUID componentProductId,

        @NotNull(message = "Component quantity is required")
        Integer quantity
    ) {}
}
