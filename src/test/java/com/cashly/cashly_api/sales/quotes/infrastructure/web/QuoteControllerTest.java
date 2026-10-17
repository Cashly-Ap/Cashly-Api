package com.cashly.cashly_api.sales.quotes.infrastructure.web;

import com.cashly.cashly_api.sales.quotes.application.cancel.request.CancelQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.create.request.CreateQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.update.request.UpdateQuoteRequest;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.sales.quotes.application.dto.response.QuoteListResponse;
import com.cashly.cashly_api.sales.quotes.application.cancel.use_case.CancelQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.create.use_case.CreateQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.delete.use_case.DeleteQuoteUseCase;
import com.cashly.cashly_api.sales.quotes.application.get.use_case.GetQuoteByIdUseCase;
import com.cashly.cashly_api.sales.quotes.application.list.use_case.GetQuotesUseCase;
import com.cashly.cashly_api.sales.quotes.application.update.use_case.UpdateQuoteUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteController Unit Tests")
class QuoteControllerTest {

    @Mock
    private CreateQuoteUseCase createQuoteUseCase;

    @Mock
    private GetQuotesUseCase getQuotesUseCase;

    @Mock
    private GetQuoteByIdUseCase getQuoteByIdUseCase;

    @Mock
    private UpdateQuoteUseCase updateQuoteUseCase;

    @Mock
    private DeleteQuoteUseCase deleteQuoteUseCase;

    @Mock
    private CancelQuoteUseCase cancelQuoteUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private QuoteController quoteController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should create quote and return 201")
    void shouldCreateQuote() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());
        QuoteDetailResponse response = new QuoteDetailResponse(
                UUID.randomUUID(), "2026-0001", UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator", "DRAFT", LocalDate.now(), null, BigDecimal.TEN,
                null, null, null, null, null, null,
                Instant.now(), Instant.now(), List.of()
        );

        when(createQuoteUseCase.execute(any(CreateQuoteRequest.class), any(UUID.class)))
                .thenReturn(response);

        CreateQuoteRequest request = new CreateQuoteRequest(
                UUID.randomUUID(), null, null,
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                ))
        );

        ResponseEntity<QuoteDetailResponse> result = quoteController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("DRAFT", result.getBody().status());
        verify(createQuoteUseCase).execute(any(CreateQuoteRequest.class), any(UUID.class));
    }

    @Test
    @DisplayName("should list quotes and return 200")
    void shouldListQuotes() {
        QuoteListResponse quote = new QuoteListResponse(
                UUID.randomUUID(), "2026-0001", UUID.randomUUID(), "Customer", "J-12345678-9",
                "DRAFT", LocalDate.now(), BigDecimal.TEN,
                UUID.randomUUID(), "Creator", Instant.now(), Instant.now()
        );

        when(getQuotesUseCase.execute(null, null, null, null, null, null, "createdAt", "desc"))
                .thenReturn(List.of(quote));

        ResponseEntity<List<QuoteListResponse>> result = quoteController.list(
                null, null, null, null, null, null, "createdAt", "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("2026-0001", result.getBody().get(0).quoteNumber());
        verify(getQuotesUseCase).execute(null, null, null, null, null, null, "createdAt", "desc");
    }

    @Test
    @DisplayName("should get quote by id and return 200")
    void shouldGetQuoteById() {
        UUID id = UUID.randomUUID();
        QuoteDetailResponse response = new QuoteDetailResponse(
                id, "2026-0001", UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator", "DRAFT", LocalDate.now(), null, BigDecimal.TEN,
                null, null, null, null, null, null,
                Instant.now(), Instant.now(), List.of()
        );

        when(getQuoteByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<QuoteDetailResponse> result = quoteController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getQuoteByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should update quote and return 200")
    void shouldUpdateQuote() {
        UUID id = UUID.randomUUID();
        QuoteDetailResponse response = new QuoteDetailResponse(
                id, "2026-0001", UUID.randomUUID(), "Customer", "J-12345678-9",
                UUID.randomUUID(), "Creator", "DRAFT", LocalDate.now(), "Updated", BigDecimal.TEN,
                null, null, null, null, null, null,
                Instant.now(), Instant.now(), List.of()
        );

        when(updateQuoteUseCase.execute(eq(id), any(UpdateQuoteRequest.class))).thenReturn(response);

        UpdateQuoteRequest request = new UpdateQuoteRequest(
                null, "Updated",
                List.of(new CreateQuoteRequest.QuoteLineRequest(
                        UUID.randomUUID(), BigDecimal.ONE, BigDecimal.TEN,
                        List.of(new CreateQuoteRequest.WarehouseAllocationRequest(UUID.randomUUID(), BigDecimal.ONE))
                ))
        );

        ResponseEntity<QuoteDetailResponse> result = quoteController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Updated", result.getBody().notes());
        verify(updateQuoteUseCase).execute(eq(id), any(UpdateQuoteRequest.class));
    }

    @Test
    @DisplayName("should delete quote and return 204")
    void shouldDeleteQuote() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteQuoteUseCase).execute(id);

        ResponseEntity<Void> result = quoteController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteQuoteUseCase).execute(id);
    }

    @Test
    @DisplayName("should cancel quote and return 200")
    void shouldCancelQuote() {
        UUID id = UUID.randomUUID();
        doNothing().when(cancelQuoteUseCase).execute(eq(id), any(CancelQuoteRequest.class));

        CancelQuoteRequest request = new CancelQuoteRequest("Customer cancelled");

        ResponseEntity<Void> result = quoteController.cancel(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(cancelQuoteUseCase).execute(eq(id), any(CancelQuoteRequest.class));
    }

    @Test
    @DisplayName("should return empty list when no quotes exist")
    void shouldReturnEmptyListWhenNoQuotesExist() {
        when(getQuotesUseCase.execute(null, null, null, null, null, null, "createdAt", "desc"))
                .thenReturn(List.of());

        ResponseEntity<List<QuoteListResponse>> result = quoteController.list(
                null, null, null, null, null, null, "createdAt", "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }
}
