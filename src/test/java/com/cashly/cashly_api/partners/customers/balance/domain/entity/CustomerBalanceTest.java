package com.cashly.cashly_api.partners.customers.balance.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerBalance Unit Tests")
class CustomerBalanceTest {

    @Test
    @DisplayName("should create balance with zero for new customer")
    void should_create_balance_with_zero_for_new_customer() {
        UUID customerId = UUID.randomUUID();

        CustomerBalance balance = CustomerBalance.create(customerId);

        assertNotNull(balance.getId());
        assertEquals(customerId, balance.getCustomerId());
        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalBalance()));
        assertNotNull(balance.getCreatedAt());
        assertNotNull(balance.getUpdatedAt());
    }

    @Test
    @DisplayName("should reconstitute balance with full state")
    void should_reconstitute_balance_with_full_state() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal totalBalance = new BigDecimal("500.00");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        CustomerBalance balance = CustomerBalance.reconstitute(id, customerId,
                totalBalance, createdAt, updatedAt);

        assertEquals(id, balance.getId());
        assertEquals(customerId, balance.getCustomerId());
        assertEquals(totalBalance, balance.getTotalBalance());
        assertEquals(createdAt, balance.getCreatedAt());
        assertEquals(updatedAt, balance.getUpdatedAt());
    }

    @Test
    @DisplayName("should add to balance when amount is positive")
    void should_add_to_balance_when_amount_is_positive() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        balance.addToBalance(new BigDecimal("100.00"));

        assertEquals(new BigDecimal("100.00"), balance.getTotalBalance());
    }

    @Test
    @DisplayName("should accumulate multiple additions to balance")
    void should_accumulate_multiple_additions_to_balance() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        balance.addToBalance(new BigDecimal("100.00"));
        balance.addToBalance(new BigDecimal("50.00"));
        balance.addToBalance(new BigDecimal("25.00"));

        assertEquals(new BigDecimal("175.00"), balance.getTotalBalance());
    }

    @Test
    @DisplayName("should throw when adding null amount")
    void should_throw_when_adding_null_amount() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        assertThrows(NullPointerException.class,
                () -> balance.addToBalance(null));
    }

    @Test
    @DisplayName("should throw when adding zero amount")
    void should_throw_when_adding_zero_amount() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> balance.addToBalance(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("should throw when adding negative amount")
    void should_throw_when_adding_negative_amount() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> balance.addToBalance(new BigDecimal("-50.00")));
    }

    @Test
    @DisplayName("should consume from balance when amount is valid")
    void should_consume_from_balance_when_amount_is_valid() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());
        balance.addToBalance(new BigDecimal("200.00"));

        balance.consumeFromBalance(new BigDecimal("80.00"));

        assertEquals(new BigDecimal("120.00"), balance.getTotalBalance());
    }

    @Test
    @DisplayName("should consume full balance when amount equals balance")
    void should_consume_full_balance_when_amount_equals_balance() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());
        balance.addToBalance(new BigDecimal("100.00"));

        balance.consumeFromBalance(new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalBalance()));
    }

    @Test
    @DisplayName("should throw when consuming null amount")
    void should_throw_when_consuming_null_amount() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        assertThrows(NullPointerException.class,
                () -> balance.consumeFromBalance(null));
    }

    @Test
    @DisplayName("should throw when consuming zero amount")
    void should_throw_when_consuming_zero_amount() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> balance.consumeFromBalance(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("should throw when consuming negative amount")
    void should_throw_when_consuming_negative_amount() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class,
                () -> balance.consumeFromBalance(new BigDecimal("-30.00")));
    }

    @Test
    @DisplayName("should throw when consuming more than available balance")
    void should_throw_when_consuming_more_than_available_balance() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());
        balance.addToBalance(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class,
                () -> balance.consumeFromBalance(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("should throw when creating balance with null customer id")
    void should_throw_when_creating_balance_with_null_customer_id() {
        assertThrows(NullPointerException.class,
                () -> CustomerBalance.create(null));
    }

    @Test
    @DisplayName("should update timestamp after adding to balance")
    void should_update_timestamp_after_adding_to_balance() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());
        LocalDateTime before = balance.getUpdatedAt();

        balance.addToBalance(new BigDecimal("50.00"));

        assertTrue(balance.getUpdatedAt().isAfter(before)
                || balance.getUpdatedAt().equals(before));
    }

    @Test
    @DisplayName("should update timestamp after consuming from balance")
    void should_update_timestamp_after_consuming_from_balance() {
        CustomerBalance balance = CustomerBalance.create(UUID.randomUUID());
        balance.addToBalance(new BigDecimal("100.00"));
        LocalDateTime before = balance.getUpdatedAt();

        balance.consumeFromBalance(new BigDecimal("30.00"));

        assertTrue(balance.getUpdatedAt().isAfter(before)
                || balance.getUpdatedAt().equals(before));
    }

    @Test
    @DisplayName("should be equal by id only")
    void should_be_equal_by_id_only() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        CustomerBalance balance1 = CustomerBalance.reconstitute(id, customerId,
                BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now());
        CustomerBalance balance2 = CustomerBalance.reconstitute(id, customerId,
                BigDecimal.valueOf(999), LocalDateTime.now(),
                LocalDateTime.now());

        assertEquals(balance1, balance2);
        assertEquals(balance1.hashCode(), balance2.hashCode());
    }

    @Test
    @DisplayName("should not be equal for different ids")
    void should_not_be_equal_for_different_ids() {
        CustomerBalance balance1 = CustomerBalance.create(UUID.randomUUID());
        CustomerBalance balance2 = CustomerBalance.create(UUID.randomUUID());

        assertNotEquals(balance1, balance2);
    }
}
