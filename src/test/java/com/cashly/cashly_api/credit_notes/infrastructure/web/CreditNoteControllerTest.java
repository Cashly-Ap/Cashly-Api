package com.cashly.cashly_api.credit_notes.infrastructure.web;

import com.cashly.cashly_api.credit_notes.application.dto.request.CreateCreditNoteRequest;
import com.cashly.cashly_api.credit_notes.application.dto.response.CreditNoteResponse;
import com.cashly.cashly_api.credit_notes.application.usecases.CreateCreditNoteUseCase;
import com.cashly.cashly_api.credit_notes.application.usecases.DeleteCreditNoteUseCase;
import com.cashly.cashly_api.credit_notes.application.usecases.GetCreditNoteByIdUseCase;
import com.cashly.cashly_api.credit_notes.application.usecases.GetCreditNotesUseCase;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditNoteController Unit Tests")
class CreditNoteControllerTest {

    @Mock
    private CreateCreditNoteUseCase createCreditNoteUseCase;

    @Mock
    private GetCreditNotesUseCase getCreditNotesUseCase;

    @Mock
    private GetCreditNoteByIdUseCase getCreditNoteByIdUseCase;

    @Mock
    private DeleteCreditNoteUseCase deleteCreditNoteUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CreditNoteController creditNoteController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @Test
    @DisplayName("should create credit note and return 201")
    void should_create_credit_note_and_return_201() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        UUID id = UUID.randomUUID();
        CreditNoteResponse response = new CreditNoteResponse(
                id, "NC-00001", UUID.randomUUID(), "Customer", "J-123",
                UUID.randomUUID(), "FAC-00001", BigDecimal.TEN, "Reason",
                authenticatedUserId, "Test User", LocalDateTime.now(), LocalDateTime.now());

        when(createCreditNoteUseCase.execute(any(CreateCreditNoteRequest.class), any(UUID.class)))
                .thenReturn(response);

        CreateCreditNoteRequest request = new CreateCreditNoteRequest(
                UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "Reason");

        ResponseEntity<CreditNoteResponse> result = creditNoteController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(createCreditNoteUseCase).execute(any(CreateCreditNoteRequest.class), eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should list credit notes and return 200")
    void should_list_credit_notes_and_return_200() {
        CreditNoteResponse response = new CreditNoteResponse(
                UUID.randomUUID(), "NC-00001", UUID.randomUUID(), "Customer", "J-123",
                UUID.randomUUID(), "FAC-00001", BigDecimal.TEN, "Reason",
                UUID.randomUUID(), "User", LocalDateTime.now(), LocalDateTime.now());

        when(getCreditNotesUseCase.execute(any(), any(), any())).thenReturn(List.of(response));

        ResponseEntity<List<CreditNoteResponse>> result = creditNoteController.list(null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(getCreditNotesUseCase).execute(null, null, null);
    }

    @Test
    @DisplayName("should return empty list when no credit notes")
    void should_return_empty_list_when_no_credit_notes() {
        when(getCreditNotesUseCase.execute(any(), any(), any())).thenReturn(List.of());

        ResponseEntity<List<CreditNoteResponse>> result = creditNoteController.list(null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getCreditNotesUseCase).execute(null, null, null);
    }

    @Test
    @DisplayName("should get credit note by id and return 200")
    void should_get_credit_note_by_id_and_return_200() {
        UUID id = UUID.randomUUID();
        CreditNoteResponse response = new CreditNoteResponse(
                id, "NC-00001", UUID.randomUUID(), "Customer", "J-123",
                UUID.randomUUID(), "FAC-00001", BigDecimal.TEN, "Reason",
                UUID.randomUUID(), "User", LocalDateTime.now(), LocalDateTime.now());

        when(getCreditNoteByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<CreditNoteResponse> result = creditNoteController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getCreditNoteByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should delete credit note and return 204")
    void should_delete_credit_note_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteCreditNoteUseCase).execute(id);

        ResponseEntity<Void> result = creditNoteController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteCreditNoteUseCase).execute(id);
    }
}
