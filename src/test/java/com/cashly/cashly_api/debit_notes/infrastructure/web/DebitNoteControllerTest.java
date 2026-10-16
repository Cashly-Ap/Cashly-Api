package com.cashly.cashly_api.debit_notes.infrastructure.web;

import com.cashly.cashly_api.debit_notes.application.create.request.CreateDebitNoteRequest;
import com.cashly.cashly_api.debit_notes.application.dto.response.DebitNoteResponse;
import com.cashly.cashly_api.debit_notes.application.create.use_case.CreateDebitNoteUseCase;
import com.cashly.cashly_api.debit_notes.application.delete.use_case.DeleteDebitNoteUseCase;
import com.cashly.cashly_api.debit_notes.application.get.use_case.GetDebitNoteByIdUseCase;
import com.cashly.cashly_api.debit_notes.application.list.use_case.GetDebitNotesUseCase;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DebitNoteController Unit Tests")
class DebitNoteControllerTest {

    @Mock
    private CreateDebitNoteUseCase createDebitNoteUseCase;

    @Mock
    private GetDebitNotesUseCase getDebitNotesUseCase;

    @Mock
    private GetDebitNoteByIdUseCase getDebitNoteByIdUseCase;

    @Mock
    private DeleteDebitNoteUseCase deleteDebitNoteUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DebitNoteController debitNoteController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @Test
    @DisplayName("should create debit note and return 201")
    void should_create_debit_note_and_return_201() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());

        UUID id = UUID.randomUUID();
        DebitNoteResponse response = new DebitNoteResponse(
                id, "ND-00001", UUID.randomUUID(), "Customer", "J-123",
                UUID.randomUUID(), "FAC-00001", BigDecimal.TEN, "Reason",
                authenticatedUserId, "Test User", LocalDateTime.now(), LocalDateTime.now());

        when(createDebitNoteUseCase.execute(any(CreateDebitNoteRequest.class), any(UUID.class)))
                .thenReturn(response);

        CreateDebitNoteRequest request = new CreateDebitNoteRequest(
                UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, "Reason");

        ResponseEntity<DebitNoteResponse> result = debitNoteController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(createDebitNoteUseCase).execute(any(CreateDebitNoteRequest.class), eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should list debit notes and return 200")
    void should_list_debit_notes_and_return_200() {
        DebitNoteResponse response = new DebitNoteResponse(
                UUID.randomUUID(), "ND-00001", UUID.randomUUID(), "Customer", "J-123",
                UUID.randomUUID(), "FAC-00001", BigDecimal.TEN, "Reason",
                UUID.randomUUID(), "User", LocalDateTime.now(), LocalDateTime.now());

        when(getDebitNotesUseCase.execute(any(), any(), any())).thenReturn(List.of(response));

        ResponseEntity<List<DebitNoteResponse>> result = debitNoteController.list(null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(getDebitNotesUseCase).execute(null, null, null);
    }

    @Test
    @DisplayName("should return empty list when no debit notes")
    void should_return_empty_list_when_no_debit_notes() {
        when(getDebitNotesUseCase.execute(any(), any(), any())).thenReturn(List.of());

        ResponseEntity<List<DebitNoteResponse>> result = debitNoteController.list(null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getDebitNotesUseCase).execute(null, null, null);
    }

    @Test
    @DisplayName("should get debit note by id and return 200")
    void should_get_debit_note_by_id_and_return_200() {
        UUID id = UUID.randomUUID();
        DebitNoteResponse response = new DebitNoteResponse(
                id, "ND-00001", UUID.randomUUID(), "Customer", "J-123",
                UUID.randomUUID(), "FAC-00001", BigDecimal.TEN, "Reason",
                UUID.randomUUID(), "User", LocalDateTime.now(), LocalDateTime.now());

        when(getDebitNoteByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<DebitNoteResponse> result = debitNoteController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getDebitNoteByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should delete debit note and return 204")
    void should_delete_debit_note_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteDebitNoteUseCase).execute(id);

        ResponseEntity<Void> result = debitNoteController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteDebitNoteUseCase).execute(id);
    }
}
