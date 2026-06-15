package com.parsun.parsun_api.customers.application.mappers;

import com.parsun.parsun_api.customers.application.dto.response.BalanceTransactionResponse;
import com.parsun.parsun_api.customers.application.dto.response.CustomerBalanceResponse;
import com.parsun.parsun_api.customers.domain.entities.BalanceTransaction;
import com.parsun.parsun_api.customers.domain.entities.CustomerBalance;
import com.parsun.parsun_api.customers.domain.valueobjects.BalanceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BalanceMapper Unit Tests")
class BalanceMapperTest {

    @Nested
    @DisplayName("toResponse from CustomerBalance")
    class ToResponse {

        @Test
        void should_MapCustomerBalanceToResponse() {
            UUID id = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            CustomerBalance balance = CustomerBalance.reconstitute(
                    id, customerId, new BigDecimal("500.00"), now, now
            );

            CustomerBalanceResponse response = BalanceMapper.toResponse(balance);

            assertEquals(customerId, response.customerId());
            assertEquals(new BigDecimal("500.00"), response.totalBalance());
            assertEquals(now, response.createdAt());
            assertEquals(now, response.updatedAt());
        }

        @Test
        void should_MapZeroBalance() {
            UUID id = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            CustomerBalance balance = CustomerBalance.reconstitute(
                    id, customerId, BigDecimal.ZERO, now, now
            );

            CustomerBalanceResponse response = BalanceMapper.toResponse(balance);

            assertEquals(BigDecimal.ZERO, response.totalBalance());
        }
    }

    @Nested
    @DisplayName("toTransactionResponse")
    class ToTransactionResponse {

        @Test
        void should_MapBalanceTransactionToResponse() {
            UUID txId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            UUID referenceId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            BalanceTransaction transaction = BalanceTransaction.reconstitute(
                    txId, customerId, BalanceType.PAYMENTS, new BigDecimal("300.00"),
                    BalanceType.PAYMENTS, referenceId, "TX-001", "FAC-001",
                    UUID.randomUUID(), now
            );

            BalanceTransactionResponse response = BalanceMapper.toTransactionResponse(transaction);

            assertEquals(txId, response.id());
            assertEquals(customerId, response.customerId());
            assertEquals("PAYMENTS", response.type());
            assertEquals(new BigDecimal("300.00"), response.amount());
            assertEquals("PAYMENTS", response.referenceType());
            assertEquals(referenceId, response.referenceId());
            assertEquals("TX-001", response.number());
            assertEquals("FAC-001", response.referenceNumber());
            assertEquals(now, response.createdAt());
        }
    }
}
