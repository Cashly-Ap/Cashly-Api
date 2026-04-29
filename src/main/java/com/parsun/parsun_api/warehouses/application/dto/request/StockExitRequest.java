package com.parsun.parsun_api.warehouses.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record StockExitRequest(
    @NotNull
    @NotEmpty(message = "At least one item is required")
    @Valid
    List<StockBatchItem> items
) {}
