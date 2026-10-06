package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.quotes.application.dto.request.AssignQuoteWarehousesRequest;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.entities.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.quotes.domain.valueobjects.QuoteStatus;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignQuoteWarehousesUseCase Unit Tests")
class AssignQuoteWarehousesUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private CustomerWarehouseAccessRepository customerWarehouseAccessRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private AssignQuoteWarehousesUseCase assignQuoteWarehousesUseCase;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID WAREHOUSE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        assignQuoteWarehousesUseCase = new AssignQuoteWarehousesUseCase(
                quoteRepository, customerWarehouseAccessRepository, warehouseRepository
        );
    }

    @Test
    @DisplayName("should assign warehouses and transition quote to DRAFT")
    void shouldAssignWarehousesAndTransitionToDraft() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = buildCustomerDraftQuote(quoteId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(customerWarehouseAccessRepository.existsByCustomerIdAndWarehouseId(CUSTOMER_ID, WAREHOUSE_ID))
                .thenReturn(true);
        when(warehouseRepository.findById(WAREHOUSE_ID))
                .thenReturn(Optional.of(buildWarehouse(WAREHOUSE_ID)));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                quote.getLines().stream()
                        .map(line -> new AssignQuoteWarehousesRequest.LineAllocationRequest(
                                line.getId().value(),
                                List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                        WAREHOUSE_ID, line.getQuantity()))
                        ))
                        .toList()
        );

        QuoteDetailResponse response = assignQuoteWarehousesUseCase.execute(quoteId, request);

        assertEquals("DRAFT", response.status());
        assertEquals(2, response.lines().size());
        for (var line : response.lines()) {
            assertEquals(1, line.allocations().size());
            assertEquals(WAREHOUSE_ID, line.allocations().get(0).warehouseId());
        }
        verify(quoteRepository).save(quote);
    }

    @Test
    @DisplayName("should throw when quote is not found")
    void shouldThrowWhenQuoteNotFound() {
        UUID quoteId = UUID.randomUUID();

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                List.of(new AssignQuoteWarehousesRequest.LineAllocationRequest(
                        UUID.randomUUID(),
                        List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                WAREHOUSE_ID, BigDecimal.ONE))))
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                assignQuoteWarehousesUseCase.execute(quoteId, request));

        assertTrue(exception.getMessage().contains("Quote not found"));
    }

    @Test
    @DisplayName("should throw when warehouse is not accessible by customer")
    void shouldThrowWhenWarehouseNotAccessible() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = buildCustomerDraftQuote(quoteId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(customerWarehouseAccessRepository.existsByCustomerIdAndWarehouseId(CUSTOMER_ID, WAREHOUSE_ID))
                .thenReturn(false);

        QuoteLine line = quote.getLines().get(0);
        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                List.of(new AssignQuoteWarehousesRequest.LineAllocationRequest(
                        line.getId().value(),
                        List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                WAREHOUSE_ID, line.getQuantity()))))
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                assignQuoteWarehousesUseCase.execute(quoteId, request));

        assertTrue(exception.getMessage().contains("does not have access"));
    }

    @Test
    @DisplayName("should throw when line is not found in quote")
    void shouldThrowWhenLineNotFound() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = buildCustomerDraftQuote(quoteId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                List.of(new AssignQuoteWarehousesRequest.LineAllocationRequest(
                        UUID.randomUUID(),
                        List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                WAREHOUSE_ID, BigDecimal.ONE))))
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                assignQuoteWarehousesUseCase.execute(quoteId, request));

        assertTrue(exception.getMessage().contains("Quote line not found"));
    }

    @Test
    @DisplayName("should throw when allocated quantity does not equal line quantity")
    void shouldThrowWhenAllocatedQuantityMismatch() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = buildCustomerDraftQuote(quoteId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(customerWarehouseAccessRepository.existsByCustomerIdAndWarehouseId(CUSTOMER_ID, WAREHOUSE_ID))
                .thenReturn(true);
        when(warehouseRepository.findById(WAREHOUSE_ID))
                .thenReturn(Optional.of(buildWarehouse(WAREHOUSE_ID)));

        QuoteLine line = quote.getLines().get(0);
        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                List.of(new AssignQuoteWarehousesRequest.LineAllocationRequest(
                        line.getId().value(),
                        List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                WAREHOUSE_ID, line.getQuantity().subtract(BigDecimal.ONE)))))
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                assignQuoteWarehousesUseCase.execute(quoteId, request));

        assertTrue(exception.getMessage().contains("must equal line quantity"));
    }

    @Test
    @DisplayName("should throw when quote is not in CUSTOMER_DRAFT status")
    void shouldThrowWhenQuoteNotCustomerDraft() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = buildDraftQuote(quoteId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(customerWarehouseAccessRepository.existsByCustomerIdAndWarehouseId(CUSTOMER_ID, WAREHOUSE_ID))
                .thenReturn(true);
        when(warehouseRepository.findById(WAREHOUSE_ID))
                .thenReturn(Optional.of(buildWarehouse(WAREHOUSE_ID)));

        QuoteLine line = quote.getLines().get(0);
        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                List.of(new AssignQuoteWarehousesRequest.LineAllocationRequest(
                        line.getId().value(),
                        List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                WAREHOUSE_ID, line.getQuantity()))))
        );

        assertThrows(QuoteStateException.class, () ->
                assignQuoteWarehousesUseCase.execute(quoteId, request));
    }

    @Test
    @DisplayName("should throw when not all lines are covered by the request")
    void shouldThrowWhenNotAllLinesCovered() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = buildCustomerDraftQuote(quoteId);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(customerWarehouseAccessRepository.existsByCustomerIdAndWarehouseId(CUSTOMER_ID, WAREHOUSE_ID))
                .thenReturn(true);
        when(warehouseRepository.findById(WAREHOUSE_ID))
                .thenReturn(Optional.of(buildWarehouse(WAREHOUSE_ID)));

        QuoteLine firstLine = quote.getLines().get(0);
        AssignQuoteWarehousesRequest request = new AssignQuoteWarehousesRequest(
                List.of(new AssignQuoteWarehousesRequest.LineAllocationRequest(
                        firstLine.getId().value(),
                        List.of(new AssignQuoteWarehousesRequest.WarehouseAllocationRequest(
                                WAREHOUSE_ID, firstLine.getQuantity()))))
        );

        assertThrows(QuoteStateException.class, () ->
                assignQuoteWarehousesUseCase.execute(quoteId, request));
    }

    private Quote buildCustomerDraftQuote(UUID quoteId) {
        return buildQuote(quoteId, QuoteStatus.CUSTOMER_DRAFT);
    }

    private Quote buildDraftQuote(UUID quoteId) {
        return buildQuote(quoteId, QuoteStatus.DRAFT);
    }

    private Quote buildQuote(UUID quoteId, QuoteStatus status) {
        Instant now = Instant.now();
        QuoteLine line1 = QuoteLine.reconstitute(
                com.cashly.cashly_api.quotes.domain.valueobjects.QuoteLineId.of(UUID.randomUUID()),
                UUID.randomUUID(), "Product A", "PROD-001",
                new BigDecimal("2"), new BigDecimal("10.00"), new BigDecimal("20.00"), List.of());
        QuoteLine line2 = QuoteLine.reconstitute(
                com.cashly.cashly_api.quotes.domain.valueobjects.QuoteLineId.of(UUID.randomUUID()),
                UUID.randomUUID(), "Product B", "PROD-002",
                new BigDecimal("3"), new BigDecimal("5.00"), new BigDecimal("15.00"), List.of());

        return Quote.reconstitute(
                quoteId, QuoteNumber.of(2026, 1), CUSTOMER_ID, "Acme Corp", "J-12345678-9",
                UUID.randomUUID(), "Test User", status,
                LocalDate.of(2026, 12, 31), null, new BigDecimal("35.00"),
                null, null, null, null,
                null, null,
                List.of(line1, line2), now, now);
    }

    private Warehouse buildWarehouse(UUID warehouseId) {
        return Warehouse.reconstitute(warehouseId, "WH-001", "Main Warehouse", "123 Main St",
                null, null, null, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
    }
}
