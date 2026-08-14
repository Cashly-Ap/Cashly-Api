package com.parsun.parsun_api.customers.application.dto.response;

import java.util.UUID;

public record CustomerBalanceMutationResponse(
        CustomerBalanceResponse balance,
        UUID transactionId
) {
}
