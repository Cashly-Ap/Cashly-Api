package com.cashly.cashly_api.finance.billing.debit_notes.application.mappers;

import com.cashly.cashly_api.finance.billing.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.entity.DebitNote;
import com.cashly.cashly_api.finance.billing.debit_notes.domain.valueobjects.DebitNoteNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DebitNoteMapper Unit Tests")
class DebitNoteMapperTest {

    private static final DebitNoteNumber NOTE_NUMBER = DebitNoteNumber.of(2026, 1);
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID INVOICE_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = new BigDecimal("50.00");
    private static final String REASON = "Price adjustment";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "John Doe";
    private static final String CUSTOMER_NAME = "Empresa Test C.A.";
    private static final String CUSTOMER_RIF = "J-12345678-9";
    private static final String INVOICE_NUMBER = "FAC-001";

    private DebitNote createDebitNote() {
        return DebitNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);
    }

    @Nested
    @DisplayName("toDebitNoteResponse")
    class ToDebitNoteResponse {

        @Test
        void should_MapAllFields() {
            DebitNote debitNote = createDebitNote();

            DebitNoteResponse response = DebitNoteMapper.toDebitNoteResponse(
                    debitNote, CUSTOMER_NAME, CUSTOMER_RIF, INVOICE_NUMBER
            );

            assertEquals(debitNote.getId().value(), response.id());
            assertEquals(NOTE_NUMBER.value(), response.noteNumber());
            assertEquals(CUSTOMER_ID, response.customerId());
            assertEquals(CUSTOMER_NAME, response.customerName());
            assertEquals(CUSTOMER_RIF, response.customerRif());
            assertEquals(INVOICE_ID, response.invoiceId());
            assertEquals(INVOICE_NUMBER, response.invoiceNumber());
            assertEquals(AMOUNT, response.amount());
            assertEquals(REASON, response.reason());
            assertEquals(CREATED_BY, response.createdBy());
            assertEquals(CREATED_BY_NAME, response.createdByName());
            assertNotNull(response.createdAt());
            assertNotNull(response.updatedAt());
        }

        @Test
        void should_MapWithNullRelations() {
            DebitNote debitNote = createDebitNote();

            DebitNoteResponse response = DebitNoteMapper.toDebitNoteResponse(
                    debitNote, null, null, null
            );

            assertNull(response.customerName());
            assertNull(response.customerRif());
            assertNull(response.invoiceNumber());
        }

        @Test
        void should_PreserveAmountAndReason() {
            DebitNote debitNote = createDebitNote();

            DebitNoteResponse response = DebitNoteMapper.toDebitNoteResponse(
                    debitNote, CUSTOMER_NAME, CUSTOMER_RIF, INVOICE_NUMBER
            );

            assertEquals(AMOUNT, response.amount());
            assertEquals(REASON, response.reason());
        }
    }
}
