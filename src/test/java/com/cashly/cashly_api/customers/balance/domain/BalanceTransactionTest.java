package com.cashly.cashly_api.customers.balance.domain;

import com.cashly.cashly_api.customers.balance.domain.valueobjects.BalanceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BalanceTransaction Unit Tests")
class BalanceTransactionTest {

    @Test
    @DisplayName("should create advance transaction")
    void should_create_advance_transaction() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction transaction = BalanceTransaction.create(
                customerId, BalanceType.ADVANCED, new BigDecimal("500.00"),
                BalanceType.ADVANCED, null, null, null, createdBy);

        assertNotNull(transaction.getId());
        assertEquals(customerId, transaction.getCustomerId());
        assertEquals(BalanceType.ADVANCED, transaction.getType());
        assertEquals(new BigDecimal("500.00"), transaction.getAmount());
        assertEquals(BalanceType.ADVANCED, transaction.getReferenceType());
        assertNull(transaction.getReferenceId());
        assertNull(transaction.getNumber());
        assertNull(transaction.getReferenceNumber());
        assertEquals(createdBy, transaction.getCreatedBy());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    @DisplayName("should create refund transaction")
    void should_create_refund_transaction() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction transaction = BalanceTransaction.create(
                customerId, BalanceType.REFUND, new BigDecimal("200.00"),
                BalanceType.REFUND, null, null, null, createdBy);

        assertEquals(BalanceType.REFUND, transaction.getType());
        assertEquals(new BigDecimal("200.00"), transaction.getAmount());
        assertEquals(BalanceType.REFUND, transaction.getReferenceType());
    }

    @Test
    @DisplayName("should create return transaction")
    void should_create_return_transaction() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction transaction = BalanceTransaction.create(
                customerId, BalanceType.RETURNS, new BigDecimal("150.00"),
                BalanceType.RETURNS, null, null, null, createdBy);

        assertEquals(BalanceType.RETURNS, transaction.getType());
        assertEquals(new BigDecimal("150.00"), transaction.getAmount());
    }

    @Test
    @DisplayName("should create payment transaction")
    void should_create_payment_transaction() {
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction transaction = BalanceTransaction.create(
                customerId, BalanceType.PAYMENTS, new BigDecimal("1000.00"),
                BalanceType.PAYMENTS, null, null, null, createdBy);

        assertEquals(BalanceType.PAYMENTS, transaction.getType());
        assertEquals(new BigDecimal("1000.00"), transaction.getAmount());
    }

    @Test
    @DisplayName("should create invoice transaction")
    void should_create_invoice_transaction() {
        UUID customerId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction transaction = BalanceTransaction.create(
                customerId, BalanceType.INVOICES, new BigDecimal("750.00"),
                BalanceType.INVOICES, referenceId, "INV-001", "INV-001", createdBy);

        assertEquals(BalanceType.INVOICES, transaction.getType());
        assertEquals(referenceId, transaction.getReferenceId());
        assertEquals("INV-001", transaction.getNumber());
        assertEquals("INV-001", transaction.getReferenceNumber());
    }

    @Test
    @DisplayName("should create credit note transaction")
    void should_create_credit_note_transaction() {
        UUID customerId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction transaction = BalanceTransaction.create(
                customerId, BalanceType.CREDIT_NOTE, new BigDecimal("300.00"),
                BalanceType.CREDIT_NOTE, referenceId, "NC-001", "INV-001", createdBy);

        assertEquals(BalanceType.CREDIT_NOTE, transaction.getType());
        assertEquals(referenceId, transaction.getReferenceId());
        assertEquals("NC-001", transaction.getNumber());
        assertEquals("INV-001", transaction.getReferenceNumber());
    }

    @Test
    @DisplayName("should reconstitute transaction with full state")
    void should_reconstitute_transaction_with_full_state() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        BalanceTransaction transaction = BalanceTransaction.reconstitute(
                id, customerId, BalanceType.ADVANCED, new BigDecimal("500.00"),
                BalanceType.ADVANCED, null, "ADV-001", null, createdBy, createdAt);

        assertEquals(id, transaction.getId());
        assertEquals(customerId, transaction.getCustomerId());
        assertEquals(BalanceType.ADVANCED, transaction.getType());
        assertEquals(new BigDecimal("500.00"), transaction.getAmount());
        assertEquals("ADV-001", transaction.getNumber());
        assertEquals(createdBy, transaction.getCreatedBy());
        assertEquals(createdAt, transaction.getCreatedAt());
    }

    @Test
    @DisplayName("should throw when customer id is null")
    void should_throw_when_customer_id_is_null() {
        assertThrows(NullPointerException.class, () -> BalanceTransaction.create(
                null, BalanceType.ADVANCED, BigDecimal.TEN,
                BalanceType.ADVANCED, null, null, null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("should throw when type is null")
    void should_throw_when_type_is_null() {
        assertThrows(NullPointerException.class, () -> BalanceTransaction.create(
                UUID.randomUUID(), null, BigDecimal.TEN,
                BalanceType.ADVANCED, null, null, null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("should throw when amount is null")
    void should_throw_when_amount_is_null() {
        assertThrows(NullPointerException.class, () -> BalanceTransaction.create(
                UUID.randomUUID(), BalanceType.ADVANCED, null,
                BalanceType.ADVANCED, null, null, null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("should throw when reference type is null")
    void should_throw_when_reference_type_is_null() {
        assertThrows(NullPointerException.class, () -> BalanceTransaction.create(
                UUID.randomUUID(), BalanceType.ADVANCED, BigDecimal.TEN,
                null, null, null, null, UUID.randomUUID()));
    }

    @Test
    @DisplayName("should throw when created by is null")
    void should_throw_when_created_by_is_null() {
        assertThrows(NullPointerException.class, () -> BalanceTransaction.create(
                UUID.randomUUID(), BalanceType.ADVANCED, BigDecimal.TEN,
                BalanceType.ADVANCED, null, null, null, null));
    }

    @Test
    @DisplayName("should be equal by id only")
    void should_be_equal_by_id_only() {
        UUID id = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction tx1 = BalanceTransaction.reconstitute(
                id, UUID.randomUUID(), BalanceType.ADVANCED, BigDecimal.TEN,
                BalanceType.ADVANCED, null, null, null, createdBy, LocalDateTime.now());
        BalanceTransaction tx2 = BalanceTransaction.reconstitute(
                id, UUID.randomUUID(), BalanceType.REFUND, BigDecimal.ONE,
                BalanceType.REFUND, null, null, null, createdBy, LocalDateTime.now());

        assertEquals(tx1, tx2);
        assertEquals(tx1.hashCode(), tx2.hashCode());
    }

    @Test
    @DisplayName("should not be equal for different ids")
    void should_not_be_equal_for_different_ids() {
        UUID createdBy = UUID.randomUUID();

        BalanceTransaction tx1 = BalanceTransaction.create(
                UUID.randomUUID(), BalanceType.ADVANCED, BigDecimal.TEN,
                BalanceType.ADVANCED, null, null, null, createdBy);
        BalanceTransaction tx2 = BalanceTransaction.create(
                UUID.randomUUID(), BalanceType.ADVANCED, BigDecimal.TEN,
                BalanceType.ADVANCED, null, null, null, createdBy);

        assertNotEquals(tx1, tx2);
    }
}
