package com.parsun.parsun_api.quotes.domain.entities;

import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quote Aggregate Root Unit Tests")
class QuoteTest {

    private static final QuoteNumber QUOTE_NUMBER = QuoteNumber.of(2025, 1);
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final String CUSTOMER_NAME = "Acme Corp";
    private static final String CUSTOMER_RIF = "J-12345678-9";
    private static final UUID CREATED_BY = UUID.randomUUID();
    private static final String CREATED_BY_NAME = "John Doe";

    private QuoteLine createLine(String name, BigDecimal qty, BigDecimal price) {
        return new QuoteLine(UUID.randomUUID(), name, "CODE-" + name, qty, price);
    }

    private List<QuoteLine> validLines() {
        return List.of(
                createLine("Product A", new BigDecimal("2"), new BigDecimal("10.00")),
                createLine("Product B", new BigDecimal("3"), new BigDecimal("5.00"))
        );
    }

    private Quote createDraftQuote() {
        return Quote.create(QUOTE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                CREATED_BY, CREATED_BY_NAME, null, null, validLines());
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create quote in DRAFT status")
        void should_create_quote_in_draft_status() {
            Quote quote = Quote.create(QUOTE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                    CREATED_BY, CREATED_BY_NAME, null, null, validLines());

            assertNotNull(quote.getId());
            assertEquals(QUOTE_NUMBER, quote.getQuoteNumber());
            assertEquals(CUSTOMER_ID, quote.getCustomerId());
            assertEquals(CUSTOMER_NAME, quote.getCustomerName());
            assertEquals(CUSTOMER_RIF, quote.getCustomerRif());
            assertEquals(CREATED_BY, quote.getCreatedBy());
            assertEquals(CREATED_BY_NAME, quote.getCreatedByName());
            assertEquals(QuoteStatus.DRAFT, quote.getStatus());
            assertNull(quote.getExpiryDate());
            assertNull(quote.getNotes());
        }

        @Test
        @DisplayName("should throw when lines is null")
        void should_throw_when_lines_null() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Quote.create(QUOTE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                            CREATED_BY, CREATED_BY_NAME, null, null, null));

            assertTrue(exception.getMessage().contains("must have at least one line"));
        }

        @Test
        @DisplayName("should throw when lines is empty")
        void should_throw_when_lines_empty() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Quote.create(QUOTE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                            CREATED_BY, CREATED_BY_NAME, null, null, Collections.emptyList()));

            assertTrue(exception.getMessage().contains("must have at least one line"));
        }

        @Test
        @DisplayName("should throw when customer id is null")
        void should_throw_when_customer_id_null() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Quote.create(QUOTE_NUMBER, null, CUSTOMER_NAME, CUSTOMER_RIF,
                            CREATED_BY, CREATED_BY_NAME, null, null, validLines()));

            assertTrue(exception.getMessage().contains("customerId"));
        }

        @Test
        @DisplayName("should throw when customer name is null")
        void should_throw_when_customer_name_null() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Quote.create(QUOTE_NUMBER, CUSTOMER_ID, null, CUSTOMER_RIF,
                            CREATED_BY, CREATED_BY_NAME, null, null, validLines()));

            assertTrue(exception.getMessage().contains("customerName"));
        }

        @Test
        @DisplayName("should throw when quote number is null")
        void should_throw_when_quote_number_null() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    Quote.create(null, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                            CREATED_BY, CREATED_BY_NAME, null, null, validLines()));

            assertTrue(exception.getMessage().contains("quoteNumber"));
        }

        @Test
        @DisplayName("should calculate subtotal on creation")
        void should_calculate_subtotal_on_creation() {
            Quote quote = Quote.create(QUOTE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                    CREATED_BY, CREATED_BY_NAME, null, null, validLines());

            
            assertEquals(new BigDecimal("35.00"), quote.getSubtotal());
        }

        @Test
        @DisplayName("should set created_at and updated_at")
        void should_set_created_at_and_updated_at() {
            Instant before = Instant.now();
            Quote quote = Quote.create(QUOTE_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                    CREATED_BY, CREATED_BY_NAME, null, null, validLines());
            Instant after = Instant.now();

            assertNotNull(quote.getCreatedAt());
            assertNotNull(quote.getUpdatedAt());
            assertFalse(quote.getCreatedAt().isBefore(before));
            assertFalse(quote.getCreatedAt().isAfter(after));
            assertEquals(quote.getCreatedAt(), quote.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("should cancel quote from DRAFT")
        void should_cancel_quote_from_draft() {
            Quote quote = createDraftQuote();
            quote.cancel("No longer needed");

            assertEquals(QuoteStatus.CANCELLED, quote.getStatus());
        }

        @Test
        @DisplayName("should set cancellation details on cancel")
        void should_set_cancellation_details_on_cancel() {
            Quote quote = createDraftQuote();
            String reason = "Out of stock";

            quote.cancel(reason);

            assertNotNull(quote.getCancelledAt());
            assertEquals(reason, quote.getCancelledReason());
        }

        @Test
        @DisplayName("should throw when cancelling with null reason")
        void should_throw_when_cancelling_with_null_reason() {
            Quote quote = createDraftQuote();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    quote.cancel(null));

            assertTrue(exception.getMessage().contains("reason"));
        }

        @Test
        @DisplayName("should throw when cancelling with blank reason")
        void should_throw_when_cancelling_with_blank_reason() {
            Quote quote = createDraftQuote();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    quote.cancel("   "));

            assertTrue(exception.getMessage().contains("reason"));
        }

        @Test
        @DisplayName("should throw when cancelling CANCELLED")
        void should_throw_when_cancelling_cancelled() {
            Quote quote = createDraftQuote();
            quote.cancel("First cancel");

            assertThrows(QuoteStateException.class, () -> quote.cancel("Second cancel"));
        }

        @Test
        @DisplayName("should mark as converted to order from DRAFT")
        void should_mark_converted_to_order_from_draft() {
            Quote quote = createDraftQuote();
            UUID orderId = UUID.randomUUID();
            String orderNumber = "ORD-2025-0001";

            quote.markConvertedToOrder(orderId, orderNumber);

            assertEquals(QuoteStatus.CONVERTED_TO_ORDER, quote.getStatus());
            assertEquals(orderId, quote.getOrderId());
            assertEquals(orderNumber, quote.getOrderNumber());
        }

        @Test
        @DisplayName("should throw when marking converted to order from non-DRAFT")
        void should_throw_when_marking_converted_to_order_from_non_draft() {
            Quote quote = createDraftQuote();
            quote.cancel("reason");

            assertThrows(QuoteStateException.class,
                    () -> quote.markConvertedToOrder(UUID.randomUUID(), "ORD-001"));
        }

        @Test
        @DisplayName("should mark as converted to invoice from DRAFT")
        void should_mark_converted_to_invoice_from_draft() {
            Quote quote = createDraftQuote();
            UUID invoiceId = UUID.randomUUID();
            String invoiceNumber = "INV-2025-0001";

            quote.markConvertedToInvoice(invoiceId, invoiceNumber);

            assertEquals(QuoteStatus.CONVERTED_TO_INVOICE, quote.getStatus());
            assertEquals(invoiceId, quote.getInvoiceId());
            assertEquals(invoiceNumber, quote.getInvoiceNumber());
        }

        @Test
        @DisplayName("should throw when marking converted to invoice from non-DRAFT")
        void should_throw_when_marking_converted_to_invoice_from_non_draft() {
            Quote quote = createDraftQuote();
            quote.cancel("reason");

            assertThrows(QuoteStateException.class,
                    () -> quote.markConvertedToInvoice(UUID.randomUUID(), "INV-001"));
        }
    }

    @Nested
    @DisplayName("Line Management")
    class LineManagement {

        @Test
        @DisplayName("should add line in DRAFT")
        void should_add_line_in_draft() {
            Quote quote = createDraftQuote();
            int initialSize = quote.getLines().size();
            QuoteLine newLine = createLine("Product C", new BigDecimal("1"), new BigDecimal("50.00"));

            quote.addLine(newLine);

            assertEquals(initialSize + 1, quote.getLines().size());
            assertTrue(quote.getLines().contains(newLine));
        }

        @Test
        @DisplayName("should throw when adding line not DRAFT")
        void should_throw_when_add_line_not_draft() {
            Quote quote = createDraftQuote();
            quote.cancel("reason");

            QuoteLine newLine = createLine("Product C", new BigDecimal("1"), new BigDecimal("50.00"));
            assertThrows(QuoteStateException.class, () -> quote.addLine(newLine));
        }

        @Test
        @DisplayName("should remove line in DRAFT")
        void should_remove_line_in_draft() {
            Quote quote = createDraftQuote();
            int initialSize = quote.getLines().size();
            QuoteLine firstLine = quote.getLines().get(0);

            quote.removeLine(firstLine.getId());

            assertEquals(initialSize - 1, quote.getLines().size());
        }

        @Test
        @DisplayName("should update line in DRAFT")
        void should_update_line_in_draft() {
            Quote quote = createDraftQuote();
            QuoteLine firstLine = quote.getLines().get(0);

            quote.updateLine(firstLine.getId(), new BigDecimal("10"), new BigDecimal("3.00"));

            QuoteLine updated = quote.getLines().get(0);
            assertEquals(new BigDecimal("10"), updated.getQuantity());
            assertEquals(new BigDecimal("3.00"), updated.getUnitPrice());
        }

        @Test
        @DisplayName("should recalculate subtotal after line changes")
        void should_recalculate_subtotal_after_line_changes() {
            Quote quote = createDraftQuote();
            BigDecimal initialSubtotal = quote.getSubtotal();

            
            QuoteLine newLine = createLine("Product C", new BigDecimal("1"), new BigDecimal("50.00"));
            quote.addLine(newLine);
            assertTrue(quote.getSubtotal().compareTo(initialSubtotal) > 0);

            
            QuoteLine firstLine = quote.getLines().get(0);
            quote.updateLine(firstLine.getId(), new BigDecimal("100"), new BigDecimal("1000.00"));
            assertTrue(quote.getSubtotal().compareTo(initialSubtotal) > 0);

            
            BigDecimal afterUpdate = quote.getSubtotal();
            quote.removeLine(newLine.getId());
            assertTrue(quote.getSubtotal().compareTo(afterUpdate) < 0);
        }
    }

    @Nested
    @DisplayName("Guards and Helpers")
    class GuardsAndHelpers {

        @Test
        @DisplayName("should return unmodifiable lines list")
        void should_return_unmodifiable_lines_list() {
            Quote quote = createDraftQuote();

            QuoteLine newLine = createLine("Product C", new BigDecimal("1"), new BigDecimal("50.00"));
            assertThrows(UnsupportedOperationException.class, () ->
                    quote.getLines().add(newLine));
        }

        @Test
        @DisplayName("should allow modification when DRAFT")
        void should_allow_modification_when_draft() {
            Quote quote = createDraftQuote();

            assertTrue(quote.canBeModified());
        }

        @Test
        @DisplayName("should prevent modification when CANCELLED")
        void should_prevent_modification_when_cancelled() {
            Quote quote = createDraftQuote();
            quote.cancel("reason");

            assertFalse(quote.canBeModified());
        }

        @Test
        @DisplayName("should allow deletion when DRAFT")
        void should_allow_deletion_when_draft() {
            Quote quote = createDraftQuote();

            assertTrue(quote.canBeDeleted());
        }

        @Test
        @DisplayName("should prevent deletion when CANCELLED")
        void should_prevent_deletion_when_cancelled() {
            Quote quote = createDraftQuote();
            quote.cancel("reason");

            assertFalse(quote.canBeDeleted());
        }

        @Test
        @DisplayName("should prevent deletion when CONVERTED_TO_ORDER")
        void should_prevent_deletion_when_converted_to_order() {
            Quote quote = createDraftQuote();
            quote.markConvertedToOrder(UUID.randomUUID(), "ORD-001");

            assertFalse(quote.canBeDeleted());
        }

        @Test
        @DisplayName("should prevent deletion when CONVERTED_TO_INVOICE")
        void should_prevent_deletion_when_converted_to_invoice() {
            Quote quote = createDraftQuote();
            quote.markConvertedToInvoice(UUID.randomUUID(), "INV-001");

            assertFalse(quote.canBeDeleted());
        }

        @Test
        @DisplayName("should report isActive correctly")
        void should_report_isActive_correctly() {
            Quote draft = createDraftQuote();
            assertTrue(draft.isActive());

            Quote convertedToOrder = createDraftQuote();
            convertedToOrder.markConvertedToOrder(UUID.randomUUID(), "ORD-001");
            assertTrue(convertedToOrder.isActive());

            Quote convertedToInvoice = createDraftQuote();
            convertedToInvoice.markConvertedToInvoice(UUID.randomUUID(), "INV-001");
            assertTrue(convertedToInvoice.isActive());

            Quote cancelled = createDraftQuote();
            cancelled.cancel("Reason");
            assertFalse(cancelled.isActive());
        }
    }

    @Nested
    @DisplayName("Reconstitution")
    class Reconstitution {

        @Test
        @DisplayName("should reconstitute quote with all fields")
        void should_reconstitute_quote_with_all_fields() {
            UUID id = UUID.randomUUID();
            QuoteNumber qn = QuoteNumber.of(2025, 42);
            UUID orderId = UUID.randomUUID();
            UUID invoiceId = UUID.randomUUID();
            Instant cancelledAt = Instant.now().minusSeconds(3600);
            Instant createdAt = Instant.now().minusSeconds(7200);
            Instant updatedAt = Instant.now().minusSeconds(3600);
            List<QuoteLine> lines = validLines();

            Quote quote = Quote.reconstitute(id, qn, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_RIF,
                    CREATED_BY, CREATED_BY_NAME,
                    QuoteStatus.CANCELLED,
                    LocalDate.of(2025, 12, 31), "Test notes", new BigDecimal("100.00"),
                    orderId, "ORD-2025-0042", invoiceId, "INV-2025-0042",
                    cancelledAt, "No longer needed",
                    lines, createdAt, updatedAt);

            assertEquals(id, quote.getId());
            assertEquals(qn, quote.getQuoteNumber());
            assertEquals(CUSTOMER_ID, quote.getCustomerId());
            assertEquals(CUSTOMER_NAME, quote.getCustomerName());
            assertEquals(CUSTOMER_RIF, quote.getCustomerRif());
            assertEquals(CREATED_BY, quote.getCreatedBy());
            assertEquals(CREATED_BY_NAME, quote.getCreatedByName());
            assertEquals(QuoteStatus.CANCELLED, quote.getStatus());
            assertEquals(LocalDate.of(2025, 12, 31), quote.getExpiryDate());
            assertEquals("Test notes", quote.getNotes());
            assertEquals(new BigDecimal("100.00"), quote.getSubtotal());
            assertEquals(orderId, quote.getOrderId());
            assertEquals("ORD-2025-0042", quote.getOrderNumber());
            assertEquals(invoiceId, quote.getInvoiceId());
            assertEquals("INV-2025-0042", quote.getInvoiceNumber());
            assertEquals(cancelledAt, quote.getCancelledAt());
            assertEquals("No longer needed", quote.getCancelledReason());
            assertEquals(2, quote.getLines().size());
            assertEquals(createdAt, quote.getCreatedAt());
            assertEquals(updatedAt, quote.getUpdatedAt());
        }
    }
}
