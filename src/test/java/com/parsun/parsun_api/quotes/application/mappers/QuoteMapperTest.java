package com.parsun.parsun_api.quotes.application.mappers;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteAllocationDetailResponse;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteLineDetailResponse;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QuoteMapper Unit Tests")
class QuoteMapperTest {

    @Test
    @DisplayName("should map Quote to QuoteListResponse with all fields correctly")
    void shouldMapToQuoteListResponse() {
        
        Quote quote = buildDraftQuote();

        
        QuoteListResponse response = QuoteMapper.toListResponse(quote);

        
        assertNotNull(response);
        assertEquals(quote.getId(), response.id());
        assertEquals(quote.getQuoteNumber().value(), response.quoteNumber());
        assertEquals(quote.getCustomerId(), response.customerId());
        assertEquals(quote.getCustomerName(), response.customerName());
        assertEquals(quote.getCustomerRif(), response.customerRif());
        assertEquals("DRAFT", response.status());
        assertEquals(quote.getExpiryDate(), response.expiryDate());
        assertEquals(quote.getSubtotal(), response.subtotal());
        assertEquals(quote.getCreatedBy(), response.createdBy());
        assertEquals(quote.getCreatedByName(), response.createdByName());
        assertEquals(quote.getCreatedAt(), response.createdAt());
        assertEquals(quote.getUpdatedAt(), response.updatedAt());
    }

    @Test
    @DisplayName("should map Quote to QuoteDetailResponse with all fields including nested lines and allocations")
    void shouldMapToQuoteDetailResponse() {
        
        UUID warehouseId = UUID.randomUUID();
        Quote quote = buildDraftQuoteWithLines(warehouseId);

        
        QuoteDetailResponse response = QuoteMapper.toDetailResponse(quote);

        
        assertNotNull(response);
        assertEquals(quote.getId(), response.id());
        assertEquals(quote.getQuoteNumber().value(), response.quoteNumber());
        assertEquals(quote.getCustomerId(), response.customerId());
        assertEquals(quote.getCustomerName(), response.customerName());
        assertEquals(quote.getCustomerRif(), response.customerRif());
        assertEquals(quote.getCreatedBy(), response.createdBy());
        assertEquals(quote.getCreatedByName(), response.createdByName());
        assertEquals("DRAFT", response.status());
        assertEquals(quote.getExpiryDate(), response.expiryDate());
        assertEquals(quote.getNotes(), response.notes());
        assertEquals(quote.getSubtotal(), response.subtotal());
        assertNull(response.orderId());
        assertNull(response.orderNumber());
        assertNull(response.invoiceId());
        assertNull(response.invoiceNumber());
        assertNull(response.cancelledAt());
        assertNull(response.cancelledReason());
        assertEquals(quote.getCreatedAt(), response.createdAt());
        assertEquals(quote.getUpdatedAt(), response.updatedAt());

        
        assertThat(response.lines()).hasSize(1);
        QuoteLineDetailResponse lineResponse = response.lines().get(0);
        QuoteLine line = quote.getLines().get(0);
        assertEquals(line.getId().value(), lineResponse.id());
        assertEquals(line.getProductId(), lineResponse.productId());
        assertEquals(line.getProductName(), lineResponse.productName());
        assertEquals(line.getProductCode(), lineResponse.productCode());
        assertEquals(line.getQuantity(), lineResponse.quantity());
        assertEquals(line.getUnitPrice(), lineResponse.unitPrice());
        assertEquals(line.getLineTotal(), lineResponse.lineTotal());

        
        assertThat(lineResponse.allocations()).hasSize(1);
        QuoteAllocationDetailResponse allocationResponse = lineResponse.allocations().get(0);
        QuoteAllocation allocation = line.getAllocations().get(0);
        assertEquals(allocation.getId().value(), allocationResponse.id());
        assertEquals(allocation.getWarehouseId(), allocationResponse.warehouseId());
        assertEquals(allocation.getWarehouseName(), allocationResponse.warehouseName());
        assertEquals(allocation.getAllocatedQuantity(), allocationResponse.allocatedQuantity());
    }

    @Test
    @DisplayName("should map Quote with multiple lines and allocations to detail response")
    void shouldMapQuoteWithMultipleLinesToDetailResponse() {
        
        Quote quote = buildQuoteWithMultipleLines();

        
        QuoteDetailResponse response = QuoteMapper.toDetailResponse(quote);

        
        assertThat(response.lines()).hasSize(2);
        assertEquals(0, new BigDecimal("350.0000").compareTo(response.subtotal()));
    }

    @Test
    @DisplayName("should map Quote with cancelled fields to detail response")
    void shouldMapCancelledQuoteToDetailResponse() {
        
        Quote quote = buildDraftQuote();
        quote.cancel("Customer decided to cancel");
        Instant cancelledAt = quote.getCancelledAt();

        
        QuoteDetailResponse response = QuoteMapper.toDetailResponse(quote);

        
        assertEquals("CANCELLED", response.status());
        assertNotNull(response.cancelledAt());
        assertEquals("Customer decided to cancel", response.cancelledReason());
    }

    @Test
    @DisplayName("should map Quote with order linkage to detail response")
    void shouldMapQuoteWithOrderToDetailResponse() {
        
        UUID orderId = UUID.randomUUID();
        Quote quote = buildDraftQuote();
        quote.markConvertedToOrder(orderId, "2026-0001");

        
        QuoteDetailResponse response = QuoteMapper.toDetailResponse(quote);

        
        assertEquals(orderId, response.orderId());
        assertEquals("2026-0001", response.orderNumber());
        assertNull(response.invoiceId());
        assertNull(response.invoiceNumber());
        assertEquals("CONVERTED_TO_ORDER", response.status());
    }

    @Test
    @DisplayName("should map Quote with invoice linkage to detail response")
    void shouldMapQuoteWithInvoiceToDetailResponse() {
        
        UUID invoiceId = UUID.randomUUID();
        Quote quote = buildDraftQuote();
        quote.markConvertedToInvoice(invoiceId, "2026-0001");

        
        QuoteDetailResponse response = QuoteMapper.toDetailResponse(quote);

        
        assertNull(response.orderId());
        assertNull(response.orderNumber());
        assertEquals(invoiceId, response.invoiceId());
        assertEquals("2026-0001", response.invoiceNumber());
        assertEquals("CONVERTED_TO_INVOICE", response.status());
    }

    @Test
    @DisplayName("should map empty lines list")
    void shouldMapQuoteWithEmptyLines() {
        
        Quote quote = Quote.reconstitute(
                UUID.randomUUID(),
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator",
                QuoteStatus.DRAFT,
                null, null, BigDecimal.ZERO,
                null, null, null, null,
                null, null,
                List.of(),
                Instant.now(), Instant.now()
        );

        
        QuoteDetailResponse response = QuoteMapper.toDetailResponse(quote);

        
        assertThat(response.lines()).isEmpty();
    }

    

    private Quote buildDraftQuote() {
        return Quote.create(
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                LocalDate.of(2026, 6, 30),
                "Test notes",
                List.of(buildQuoteLine(UUID.randomUUID()))
        );
    }

    private Quote buildDraftQuoteWithLines(UUID warehouseId) {
        QuoteLine line = buildQuoteLine(warehouseId);
        QuoteAllocation allocation = new QuoteAllocation(
                UUID.randomUUID(), warehouseId, "Main Warehouse", BigDecimal.ONE
        );
        line.addAllocation(allocation);

        return Quote.create(
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                LocalDate.of(2026, 6, 30),
                "Test notes",
                List.of(line)
        );
    }

    private Quote buildQuoteWithMultipleLines() {
        QuoteLine line1 = buildQuoteLine(UUID.randomUUID());
        QuoteLine line2 = new QuoteLine(
                UUID.randomUUID(), "Product B", "PROD-002", BigDecimal.ONE, new BigDecimal("250.0000")
        );

        return Quote.create(
                QuoteNumber.of(2026, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                null,
                null,
                List.of(line1, line2)
        );
    }

    private QuoteLine buildQuoteLine(UUID productId) {
        return new QuoteLine(
                productId, "Product A", "PROD-001", BigDecimal.ONE, new BigDecimal("100.0000")
        );
    }
}
