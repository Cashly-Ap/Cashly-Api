package com.cashly.cashly_api.credit_notes.domain;

import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditNote Domain Entity Unit Tests")
class CreditNoteUnitTest {

    private static final CreditNoteNumber NOTE_NUMBER = CreditNoteNumber.of(2026, 1);
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID INVOICE_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = new BigDecimal("150.00");
    private static final String REASON = "Product damaged in transit";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "John Doe";

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        void should_CreateCreditNote_When_ValidDataProvided() {
            CreditNote creditNote = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            assertNotNull(creditNote.getId());
            assertEquals(NOTE_NUMBER, creditNote.getNoteNumber());
            assertEquals(CUSTOMER_ID, creditNote.getCustomerId());
            assertEquals(INVOICE_ID, creditNote.getInvoiceId());
            assertEquals(AMOUNT, creditNote.getAmount());
            assertEquals(REASON, creditNote.getReason());
            assertEquals(CREATED_BY, creditNote.getCreatedBy());
            assertEquals(CREATED_BY_NAME, creditNote.getCreatedByName());
            assertNotNull(creditNote.getCreatedAt());
            assertNotNull(creditNote.getUpdatedAt());
            assertEquals(creditNote.getCreatedAt(), creditNote.getUpdatedAt());
        }

        @Test
        void should_ThrowException_When_NoteNumberIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(null, CUSTOMER_ID, INVOICE_ID,
                            AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_CustomerIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(NOTE_NUMBER, null, INVOICE_ID,
                            AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_InvoiceIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, null,
                            AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_AmountIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            null, REASON, CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_AmountIsZero() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            BigDecimal.ZERO, REASON, CREATED_BY, CREATED_BY_NAME));

            assertEquals("Amount must be greater than zero", exception.getMessage());
        }

        @Test
        void should_ThrowException_When_AmountIsNegative() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            new BigDecimal("-50.00"), REASON, CREATED_BY, CREATED_BY_NAME));

            assertEquals("Amount must be greater than zero", exception.getMessage());
        }

        @Test
        void should_ThrowException_When_ReasonIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            AMOUNT, null, CREATED_BY, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_ReasonIsBlank() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            AMOUNT, "   ", CREATED_BY, CREATED_BY_NAME));

            assertEquals("Reason cannot be blank", exception.getMessage());
        }

        @Test
        void should_ThrowException_When_CreatedByIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            AMOUNT, REASON, null, CREATED_BY_NAME));
        }

        @Test
        void should_ThrowException_When_CreatedByNameIsNull() {
            assertThrows(NullPointerException.class, () ->
                    CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                            AMOUNT, REASON, CREATED_BY, null));
        }

        @Test
        void should_GenerateDifferentIds_When_CreatingMultipleNotes() {
            CreditNote first = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);
            CreditNote second = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            assertNotEquals(first.getId(), second.getId());
        }
    }

    @Nested
    @DisplayName("Reconstitution")
    class Reconstitution {

        @Test
        void should_ReconstituteCreditNote_When_AllFieldsProvided() {
            CreditNoteId id = CreditNoteId.generate();
            LocalDateTime now = LocalDateTime.now();

            CreditNote creditNote = CreditNote.reconstitute(id, NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME, now, now);

            assertEquals(id, creditNote.getId());
            assertEquals(NOTE_NUMBER, creditNote.getNoteNumber());
            assertEquals(CUSTOMER_ID, creditNote.getCustomerId());
            assertEquals(INVOICE_ID, creditNote.getInvoiceId());
            assertEquals(AMOUNT, creditNote.getAmount());
            assertEquals(REASON, creditNote.getReason());
            assertEquals(CREATED_BY, creditNote.getCreatedBy());
            assertEquals(CREATED_BY_NAME, creditNote.getCreatedByName());
            assertEquals(now, creditNote.getCreatedAt());
            assertEquals(now, creditNote.getUpdatedAt());
        }

        @Test
        void should_ReconstituteWithDifferentTimestamps() {
            CreditNoteId id = CreditNoteId.generate();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 15, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 16, 14, 30);

            CreditNote creditNote = CreditNote.reconstitute(id, NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME, createdAt, updatedAt);

            assertEquals(createdAt, creditNote.getCreatedAt());
            assertEquals(updatedAt, creditNote.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        void should_ReturnGeneratedId() {
            CreditNote creditNote = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            assertNotNull(creditNote.getId());
            assertInstanceOf(CreditNoteId.class, creditNote.getId());
        }

        @Test
        void should_ReturnNoteNumber() {
            CreditNote creditNote = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            assertEquals(NOTE_NUMBER, creditNote.getNoteNumber());
        }

        @Test
        void should_ReturnAmount() {
            CreditNote creditNote = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            assertEquals(AMOUNT, creditNote.getAmount());
        }

        @Test
        void should_ReturnReason() {
            CreditNote creditNote = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            assertEquals(REASON, creditNote.getReason());
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        void should_BeEqual_When_SameId() {
            CreditNoteId id = CreditNoteId.generate();
            LocalDateTime now = LocalDateTime.now();

            CreditNote first = CreditNote.reconstitute(id, NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME, now, now);
            CreditNote second = CreditNote.reconstitute(id, CreditNoteNumber.of(2026, 2),
                    UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("200.00"),
                    "Another reason", UUID.randomUUID(), "Another user", now, now);

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void should_NotBeEqual_When_DifferentIds() {
            LocalDateTime now = LocalDateTime.now();

            CreditNote first = CreditNote.reconstitute(CreditNoteId.generate(), NOTE_NUMBER,
                    CUSTOMER_ID, INVOICE_ID, AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME, now, now);
            CreditNote second = CreditNote.reconstitute(CreditNoteId.generate(), NOTE_NUMBER,
                    CUSTOMER_ID, INVOICE_ID, AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME, now, now);

            assertNotEquals(first, second);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        void should_IncludeIdAndNoteNumberAndAmountInToString() {
            CreditNote creditNote = CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                    AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);

            String result = creditNote.toString();

            assertTrue(result.contains(creditNote.getId().toString()));
            assertTrue(result.contains(NOTE_NUMBER.value()));
            assertTrue(result.contains(AMOUNT.toString()));
        }
    }
}
