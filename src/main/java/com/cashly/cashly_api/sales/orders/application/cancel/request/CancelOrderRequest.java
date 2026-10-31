package com.cashly.cashly_api.sales.orders.application.cancel.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelOrderRequest(
        @NotBlank(message = "El motivo de cancelacion es requerido") @Size(max = 500, message = "El motivo debe tener maximo 500 caracteres") String reason) {
}
