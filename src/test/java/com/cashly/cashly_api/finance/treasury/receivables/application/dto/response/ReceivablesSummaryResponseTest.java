package com.cashly.cashly_api.finance.treasury.receivables.application.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReceivablesSummaryResponse Unit Tests")
class ReceivablesSummaryResponseTest {

    @Nested
    @DisplayName("Record creation")
    class Creation {

        @Test
        void should_CreateWithAllFields() {
            LocalDateTime now = LocalDateTime.now();

            ReceivablesSummaryResponse response = new ReceivablesSummaryResponse(
                    new BigDecimal("15000.00"), 25L, 40L,
                    new BigDecimal("2000.00"), new BigDecimal("5000.00"), now
            );

            assertEquals(new BigDecimal("15000.00"), response.totalDebt());
            assertEquals(25L, response.totalCustomersWithDebt());
            assertEquals(40L, response.totalInvoicesWithDebt());
            assertEquals(new BigDecimal("2000.00"), response.totalPendingValidationAmount());
            assertEquals(new BigDecimal("5000.00"), response.totalCustomerBalance());
            assertEquals(now, response.calculatedAt());
        }
    }

    @Nested
    @DisplayName("empty() factory method")
    class EmptyFactory {

        @Test
        void should_ReturnResponseWithZeroValues() {
            ReceivablesSummaryResponse response = ReceivablesSummaryResponse.empty();

            assertEquals(BigDecimal.ZERO, response.totalDebt());
            assertEquals(0L, response.totalCustomersWithDebt());
            assertEquals(0L, response.totalInvoicesWithDebt());
            assertEquals(BigDecimal.ZERO, response.totalPendingValidationAmount());
            assertEquals(BigDecimal.ZERO, response.totalCustomerBalance());
            assertNotNull(response.calculatedAt());
        }

        @Test
        void should_SetCalculatedAtToCurrentTime() {
            ReceivablesSummaryResponse response = ReceivablesSummaryResponse.empty();

            assertNotNull(response.calculatedAt());
            assertTrue(response.calculatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("Record immutability")
    class Immutability {

        @Test
        void should_BeRecord() {
            assertTrue(ReceivablesSummaryResponse.class.isRecord());
        }

        @Test
        void should_ImplementEqualsAndHashCode() {
            LocalDateTime now = LocalDateTime.now();
            ReceivablesSummaryResponse r1 = new ReceivablesSummaryResponse(
                    BigDecimal.TEN, 1L, 2L, BigDecimal.ONE, BigDecimal.ZERO, now
            );
            ReceivablesSummaryResponse r2 = new ReceivablesSummaryResponse(
                    BigDecimal.TEN, 1L, 2L, BigDecimal.ONE, BigDecimal.ZERO, now
            );

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
        }
    }
}
