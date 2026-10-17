package com.cashly.cashly_api.inventory.warehouses.stock.application.exit.request;

import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockBatchItem;

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
