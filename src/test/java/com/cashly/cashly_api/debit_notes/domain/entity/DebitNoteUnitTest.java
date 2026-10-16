package com.cashly.cashly_api.debit_notes.domain.entity;

import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteId;
import com.cashly.cashly_api.debit_notes.domain.valueobjects.DebitNoteNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DebitNote Entity Unit Tests")
class DebitNoteUnitTest {

    private final DebitNoteNumber noteNumber = DebitNoteNumber.of(2026, 1);
    private final UUID customerId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();
    private final BigDecimal amount = BigDecimal.valueOf(1500.00);
    private final String reason = "Product defect";
    private final UUID createdBy = UUID.randomUUID();
    private final String createdByName = "John Doe";

    @Test
    void should_CreateDebitNote_When_ValidParametersProvided() {
        DebitNote note = DebitNote.create(noteNumber, customerId, invoiceId, amount, reason, createdBy, createdByName);

        assertNotNull(note.getId());
        assertEquals(noteNumber, note.getNoteNumber());
        assertEquals(customerId, note.getCustomerId());
        assertEquals(invoiceId, note.getInvoiceId());
        assertEquals(amount, note.getAmount());
        assertEquals(reason, note.getReason());
        assertEquals(createdBy, note.getCreatedBy());
        assertEquals(createdByName, note.getCreatedByName());
        assertNotNull(note.getCreatedAt());
        assertNotNull(note.getUpdatedAt());
        assertEquals(note.getCreatedAt(), note.getUpdatedAt());
    }

    @Test
    void should_ThrowException_When_NullNoteNumber() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(null, customerId, invoiceId, amount, reason, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_NullCustomerId() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(noteNumber, null, invoiceId, amount, reason, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_NullInvoiceId() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(noteNumber, customerId, null, amount, reason, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_NullAmount() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, null, reason, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_AmountIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, BigDecimal.ZERO, reason, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_AmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, BigDecimal.valueOf(-100), reason, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_NullReason() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, amount, null, createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_ReasonIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, amount, "   ", createdBy, createdByName));
    }

    @Test
    void should_ThrowException_When_NullCreatedBy() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, amount, reason, null, createdByName));
    }

    @Test
    void should_ThrowException_When_NullCreatedByName() {
        assertThrows(NullPointerException.class, () ->
                DebitNote.create(noteNumber, customerId, invoiceId, amount, reason, createdBy, null));
    }

    @Test
    void should_ReconstituteDebitNote_When_AllFieldsProvided() {
        DebitNoteId id = DebitNoteId.generate();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 16, 14, 0);

        DebitNote note = DebitNote.reconstitute(
                id, noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, createdAt, updatedAt
        );

        assertEquals(id, note.getId());
        assertEquals(noteNumber, note.getNoteNumber());
        assertEquals(customerId, note.getCustomerId());
        assertEquals(invoiceId, note.getInvoiceId());
        assertEquals(amount, note.getAmount());
        assertEquals(reason, note.getReason());
        assertEquals(createdBy, note.getCreatedBy());
        assertEquals(createdByName, note.getCreatedByName());
        assertEquals(createdAt, note.getCreatedAt());
        assertEquals(updatedAt, note.getUpdatedAt());
    }

    @Test
    void should_BeEqual_When_SameId() {
        DebitNoteId id = DebitNoteId.generate();
        DebitNote first = DebitNote.reconstitute(
                id, noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, LocalDateTime.now(), LocalDateTime.now());
        DebitNote second = DebitNote.reconstitute(
                id, noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void should_NotBeEqual_When_DifferentId() {
        DebitNote first = DebitNote.reconstitute(
                DebitNoteId.generate(), noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, LocalDateTime.now(), LocalDateTime.now());
        DebitNote second = DebitNote.reconstitute(
                DebitNoteId.generate(), noteNumber, customerId, invoiceId, amount, reason,
                createdBy, createdByName, LocalDateTime.now(), LocalDateTime.now());

        assertNotEquals(first, second);
    }

    @Test
    void should_ToString_IncludeIdAndNumberAndAmount() {
        DebitNote note = DebitNote.create(noteNumber, customerId, invoiceId, amount, reason, createdBy, createdByName);

        String str = note.toString();

        assertTrue(str.contains(note.getId().toString()));
        assertTrue(str.contains(note.getNoteNumber().value()));
        assertTrue(str.contains(note.getAmount().toString()));
    }
}
