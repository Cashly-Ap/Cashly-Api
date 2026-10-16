package com.cashly.cashly_api.credit_notes.application.mappers;

import com.cashly.cashly_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.credit_notes.domain.CreditNote;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteId;
import com.cashly.cashly_api.credit_notes.domain.valueobjects.CreditNoteNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreditNoteMapper Unit Tests")
class CreditNoteMapperTest {

    private static final CreditNoteNumber NOTE_NUMBER = CreditNoteNumber.of(2026, 1);
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID INVOICE_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = new BigDecimal("150.00");
    private static final String REASON = "Product damaged";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "John Doe";
    private static final String CUSTOMER_NAME = "Empresa Test C.A.";
    private static final String CUSTOMER_RIF = "J-12345678-9";
    private static final String INVOICE_NUMBER = "FAC-001";

    private CreditNote createCreditNote() {
        return CreditNote.create(NOTE_NUMBER, CUSTOMER_ID, INVOICE_ID,
                AMOUNT, REASON, CREATED_BY, CREATED_BY_NAME);
    }

    @Nested
    @DisplayName("toCreditNoteResponse")
    class ToCreditNoteResponse {

        @Test
        void should_MapAllFields() {
            CreditNote creditNote = createCreditNote();

            CreditNoteResponse response = CreditNoteMapper.toCreditNoteResponse(
                    creditNote, CUSTOMER_NAME, CUSTOMER_RIF, INVOICE_NUMBER
            );

            assertEquals(creditNote.getId().value(), response.id());
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
            CreditNote creditNote = createCreditNote();

            CreditNoteResponse response = CreditNoteMapper.toCreditNoteResponse(
                    creditNote, null, null, null
            );

            assertNull(response.customerName());
            assertNull(response.customerRif());
            assertNull(response.invoiceNumber());
        }

        @Test
        void should_MaintainImmutability() {
            CreditNote creditNote = createCreditNote();

            CreditNoteResponse response = CreditNoteMapper.toCreditNoteResponse(
                    creditNote, CUSTOMER_NAME, CUSTOMER_RIF, INVOICE_NUMBER
            );

            assertEquals(AMOUNT, response.amount());
            assertEquals(REASON, response.reason());
        }
    }
}
