package com.parsun.parsun_api.orders.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateOrderRequest(
        @NotNull(message = "La fecha de entrega es requerida")
        LocalDate deliveryDate,

        @Size(max = 1000, message = "Las notas deben tener maximo 1000 caracteres")
        String notes,

        @NotEmpty(message = "Se requiere al menos una linea de pedido")
        @Valid
        List<OrderLineRequest> lines
) {
    public record OrderLineRequest(
            @NotNull(message = "El producto es requerido")
            UUID productId,

            @NotNull(message = "La cantidad es requerida")
            BigDecimal quantity,

            @NotNull(message = "El precio es requerido")
            BigDecimal unitPrice
    ) {}
}
