package com.cashly.cashly_api.finance.billing.invoices.infrastructure.web;

import com.cashly.cashly_api.finance.billing.invoices.application.cancel.request.CancelInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.confirm.request.ConfirmInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.create.request.CreateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.duplicate.request.DuplicateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.update.request.UpdateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceListResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.cancel.use_case.CancelInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.confirm.use_case.ConfirmInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.create.use_case.CreateInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.delete.use_case.DeleteInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.duplicate.use_case.DuplicateInvoiceUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.get.use_case.GetInvoiceByIdUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.list.use_case.GetInvoicesUseCase;
import com.cashly.cashly_api.finance.billing.invoices.application.update.use_case.UpdateInvoiceUseCase;
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
@DisplayName("InvoiceController Unit Tests")
class InvoiceControllerTest {

    @Mock
    private CreateInvoiceUseCase createInvoiceUseCase;

    @Mock
    private GetInvoiceByIdUseCase getInvoiceByIdUseCase;

    @Mock
    private GetInvoicesUseCase getInvoicesUseCase;

    @Mock
    private CancelInvoiceUseCase cancelInvoiceUseCase;

    @Mock
    private DuplicateInvoiceUseCase duplicateInvoiceUseCase;

    @Mock
    private UpdateInvoiceUseCase updateInvoiceUseCase;

    @Mock
    private ConfirmInvoiceUseCase confirmInvoiceUseCase;

    @Mock
    private DeleteInvoiceUseCase deleteInvoiceUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InvoiceController invoiceController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @Test
    @DisplayName("should create invoice and return 201")
    void should_create_invoice_and_return_201() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());

        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);

        when(createInvoiceUseCase.execute(any(CreateInvoiceRequest.class),
                any(UUID.class))).thenReturn(response);

        var request = new CreateInvoiceRequest(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), "CREDIT", null, null,
                null, null, null, List.of());

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(createInvoiceUseCase).execute(any(CreateInvoiceRequest.class),
                eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should get all invoices and return 200")
    void should_get_all_invoices_and_return_200() {
        InvoiceListResponse invoice = new InvoiceListResponse(UUID.randomUUID(),
                "FAC-00001", UUID.randomUUID(), "Customer", "CONFIRMED",
                "Confirmed", "CREDIT", BigDecimal.TEN, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 2,
                UUID.randomUUID(), "ORD-00001", LocalDateTime.now(), null);

        when(getInvoicesUseCase.execute(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(invoice));

        ResponseEntity<List<InvoiceListResponse>> result = invoiceController
                .getAll(null, null, null, null, null, null, null, "createdAt",
                        "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(getInvoicesUseCase).execute(null, null, null, null, null, null,
                null, null, null, "createdAt", "desc");
    }

    @Test
    @DisplayName("should return empty list when no invoices")
    void should_return_empty_list_when_no_invoices() {
        when(getInvoicesUseCase.execute(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        ResponseEntity<List<InvoiceListResponse>> result = invoiceController
                .getAll(null, null, null, null, null, null, null, "createdAt",
                        "desc");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
        verify(getInvoicesUseCase).execute(null, null, null, null, null, null,
                null, null, null, "createdAt", "desc");
    }

    @Test
    @DisplayName("should get invoice by id and return 200")
    void should_get_invoice_by_id_and_return_200() {
        UUID id = UUID.randomUUID();
        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);
        when(response.id()).thenReturn(id);

        when(getInvoiceByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getInvoiceByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should cancel invoice and return 200")
    void should_cancel_invoice_and_return_200() {
        UUID id = UUID.randomUUID();
        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);

        when(cancelInvoiceUseCase.execute(eq(id),
                any(CancelInvoiceRequest.class))).thenReturn(response);

        CancelInvoiceRequest request = new CancelInvoiceRequest("Test reason");

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .cancel(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(cancelInvoiceUseCase).execute(eq(id),
                any(CancelInvoiceRequest.class));
    }

    @Test
    @DisplayName("should duplicate invoice and return 201")
    void should_duplicate_invoice_and_return_201() {
        UUID id = UUID.randomUUID();
        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);

        when(duplicateInvoiceUseCase.execute(eq(id), isNull()))
                .thenReturn(response);

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .duplicate(id, null);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(duplicateInvoiceUseCase).execute(eq(id), isNull());
    }

    @Test
    @DisplayName("should duplicate invoice with customer and return 201")
    void should_duplicate_invoice_with_customer_and_return_201() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);

        when(duplicateInvoiceUseCase.execute(eq(id), eq(customerId)))
                .thenReturn(response);

        DuplicateInvoiceRequest request = new DuplicateInvoiceRequest(
                customerId);

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .duplicate(id, request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(duplicateInvoiceUseCase).execute(eq(id), eq(customerId));
    }

    @Test
    @DisplayName("should update invoice and return 200")
    void should_update_invoice_and_return_200() {
        UUID id = UUID.randomUUID();
        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);

        when(updateInvoiceUseCase.execute(eq(id),
                any(UpdateInvoiceRequest.class))).thenReturn(response);

        var request = new UpdateInvoiceRequest("CREDIT", null, List.of());

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(updateInvoiceUseCase).execute(eq(id),
                any(UpdateInvoiceRequest.class));
    }

    @Test
    @DisplayName("should confirm invoice and return 200")
    void should_confirm_invoice_and_return_200() {
        when(authentication.getPrincipal())
                .thenReturn(authenticatedUserId.toString());

        UUID id = UUID.randomUUID();
        InvoiceDetailResponse response = mock(InvoiceDetailResponse.class);

        when(confirmInvoiceUseCase.execute(eq(id),
                any(ConfirmInvoiceRequest.class), eq(authenticatedUserId)))
                .thenReturn(response);

        ConfirmInvoiceRequest request = new ConfirmInvoiceRequest(null, null,
                null, null);

        ResponseEntity<InvoiceDetailResponse> result = invoiceController
                .confirm(id, request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(confirmInvoiceUseCase).execute(eq(id),
                any(ConfirmInvoiceRequest.class), eq(authenticatedUserId));
    }

    @Test
    @DisplayName("should delete invoice and return 204")
    void should_delete_invoice_and_return_204() {
        UUID id = UUID.randomUUID();
        doNothing().when(deleteInvoiceUseCase).execute(id);

        ResponseEntity<Void> result = invoiceController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deleteInvoiceUseCase).execute(id);
    }
    @Test
    void shouldRejectUnknownActionFilter() throws Exception {
        org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(invoiceController).build()
                .perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/invoices")
                                .param("action", "UNKNOWN"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isBadRequest());
    }

    @Test
    void shouldPassActionFilterToListQuery() throws Exception {
        org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(invoiceController).build()
                .perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/invoices").param("action", "EDIT"))
                .andExpect(
                        org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                .status().isOk());
        verify(getInvoicesUseCase).execute(null, null, null, null, null, null,
                null, null, null, "createdAt", "desc",
                com.cashly.cashly_api.shared.domain.DocumentAction.EDIT);
    }

}
