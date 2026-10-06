package com.cashly.cashly_api.warehouses.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWarehouseRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @Size(max = 100, message = "State cannot exceed 100 characters")
        String state,

        @Size(max = 100, message = "Municipality cannot exceed 100 characters")
        String municipality,

        @Size(max = 10, message = "Postal code cannot exceed 10 characters")
        String postalCode,

        Boolean active
) {}
