package com.cashly.cashly_api.partners.customers.balance.application.dto.response;

import java.util.UUID;

public record CustomerBalanceMutationResponse(
        CustomerBalanceResponse balance,
        UUID transactionId
) {
}
