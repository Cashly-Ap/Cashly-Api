package com.cashly.cashly_api.procurement.purchases.application.confirm.request;

import jakarta.validation.constraints.Size;

public record ConfirmPurchaseRequest(
        @Size(max = 200, message = "Confirmation password is too long") String confirmationPassword) {
}
