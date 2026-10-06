package com.cashly.cashly_api.payments.infrastructure.web;

import com.cashly.cashly_api.payments.application.dto.request.CreatePaymentRequest;
import com.cashly.cashly_api.payments.application.dto.request.PayCustomerDebtRequest;
import com.cashly.cashly_api.payments.application.dto.request.PayInvoiceRequest;
import com.cashly.cashly_api.payments.application.dto.request.PayPartialDebtRequest;
import com.cashly.cashly_api.payments.application.dto.request.UpdatePaymentRequest;
import com.cashly.cashly_api.payments.application.dto.response.PaymentListResponse;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResponse;
import com.cashly.cashly_api.payments.application.dto.response.PaymentResultResponse;
import com.cashly.cashly_api.payments.application.usecases.CreatePaymentUseCase;
import com.cashly.cashly_api.payments.application.usecases.DeletePaymentUseCase;
import com.cashly.cashly_api.payments.application.usecases.GetAllPaymentsUseCase;
import com.cashly.cashly_api.payments.application.usecases.GetPaymentByIdUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayAllCustomerDebtUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayInvoiceUseCase;
import com.cashly.cashly_api.payments.application.usecases.PayPartialDebtUseCase;
import com.cashly.cashly_api.payments.application.usecases.UpdatePaymentUseCase;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Mock
    private CreatePaymentUseCase createPaymentUseCase;

    @Mock
    private GetAllPaymentsUseCase getAllPaymentsUseCase;

    @Mock
    private GetPaymentByIdUseCase getPaymentByIdUseCase;

    @Mock
    private UpdatePaymentUseCase updatePaymentUseCase;

    @Mock
    private DeletePaymentUseCase deletePaymentUseCase;

    @Mock
    private PayInvoiceUseCase payInvoiceUseCase;

    @Mock
    private PayAllCustomerDebtUseCase payAllCustomerDebtUseCase;

    @Mock
    private PayPartialDebtUseCase payPartialDebtUseCase;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should create payment and return 201")
    void shouldCreatePayment() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());
        PaymentResponse response = new PaymentResponse(
                UUID.randomUUID(), "PAY-2026-00001", UUID.randomUUID(),
                BigDecimal.TEN, UUID.randomUUID(), "REF-001",
                LocalDate.now(), "Test payment", "DRAFT",
                BigDecimal.ZERO, UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now(),
                null, null, null, null,
                null, null
        );

        when(createPaymentUseCase.execute(any(CreatePaymentRequest.class), any(UUID.class)))
                .thenReturn(response);

        CreatePaymentRequest request = new CreatePaymentRequest(
                UUID.randomUUID(), "DRAFT", BigDecimal.TEN,
                UUID.randomUUID(), null, LocalDate.now(), null,
                null, null
        );

        ResponseEntity<PaymentResponse> result = paymentController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("PAY-2026-00001", result.getBody().paymentNumber());
        verify(createPaymentUseCase).execute(any(CreatePaymentRequest.class), any(UUID.class));
    }

    @Test
    @DisplayName("should list payments and return 200")
    void shouldListPayments() {
        PaymentListResponse payment = new PaymentListResponse(
                UUID.randomUUID(), "PAY-2026-00001", UUID.randomUUID(),
                BigDecimal.TEN, UUID.randomUUID(), "REF-001", LocalDate.now(),
                "APPLIED", BigDecimal.ZERO, LocalDateTime.now(),
                null, null
        );

        when(getAllPaymentsUseCase.execute(null, null, null, null, null))
                .thenReturn(List.of(payment));

        ResponseEntity<List<PaymentListResponse>> result = paymentController.getAll(
                null, null, null, null, null
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("PAY-2026-00001", result.getBody().get(0).paymentNumber());
        verify(getAllPaymentsUseCase).execute(null, null, null, null, null);
    }

    @Test
    @DisplayName("should list payments with filters and return 200")
    void shouldListPaymentsWithFilters() {
        when(getAllPaymentsUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        ResponseEntity<List<PaymentListResponse>> result = paymentController.getAll(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                UUID.randomUUID(),
                "APPLIED",
                null
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    @DisplayName("should get payment by id and return 200")
    void shouldGetPaymentById() {
        UUID id = UUID.randomUUID();
        PaymentResponse response = new PaymentResponse(
                id, "PAY-2026-00001", UUID.randomUUID(),
                BigDecimal.TEN, UUID.randomUUID(), "REF-001",
                LocalDate.now(), null, "APPLIED",
                BigDecimal.ZERO, UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now(),
                null, null, null, null,
                null, null
        );

        when(getPaymentByIdUseCase.execute(id)).thenReturn(response);

        ResponseEntity<PaymentResponse> result = paymentController.getById(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(id, result.getBody().id());
        verify(getPaymentByIdUseCase).execute(id);
    }

    @Test
    @DisplayName("should pay invoice and return 201")
    void shouldPayInvoice() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());
        when(authentication.getName()).thenReturn("Test User");
        UUID invoiceId = UUID.randomUUID();
        PaymentResultResponse response = mock(PaymentResultResponse.class);

        when(payInvoiceUseCase.execute(eq(invoiceId), any(PayInvoiceRequest.class), any(UUID.class), anyString()))
                .thenReturn(response);

        PayInvoiceRequest request = new PayInvoiceRequest(
                UUID.randomUUID(), null, LocalDate.now(), BigDecimal.TEN, null, null
        );

        ResponseEntity<PaymentResultResponse> result = paymentController.payInvoice(invoiceId, request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(payInvoiceUseCase).execute(eq(invoiceId), any(PayInvoiceRequest.class), any(UUID.class), anyString());
    }

    @Test
    @DisplayName("should pay all customer debt and return 201")
    void shouldPayAllCustomerDebt() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());
        when(authentication.getName()).thenReturn("Test User");
        UUID customerId = UUID.randomUUID();
        PaymentResultResponse response = mock(PaymentResultResponse.class);

        when(payAllCustomerDebtUseCase.execute(eq(customerId), any(PayCustomerDebtRequest.class), any(UUID.class), anyString()))
                .thenReturn(response);

        PayCustomerDebtRequest request = new PayCustomerDebtRequest(
                UUID.randomUUID(), null, LocalDate.now(), null, null
        );

        ResponseEntity<PaymentResultResponse> result = paymentController.payAllCustomerDebt(customerId, request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(payAllCustomerDebtUseCase).execute(eq(customerId), any(PayCustomerDebtRequest.class), any(UUID.class), anyString());
    }

    @Test
    @DisplayName("should pay partial debt and return 201")
    void shouldPayPartialDebt() {
        when(authentication.getPrincipal()).thenReturn(authenticatedUserId.toString());
        when(authentication.getName()).thenReturn("Test User");
        UUID customerId = UUID.randomUUID();
        PaymentResultResponse response = mock(PaymentResultResponse.class);

        when(payPartialDebtUseCase.execute(eq(customerId), any(PayPartialDebtRequest.class), any(UUID.class), anyString()))
                .thenReturn(response);

        PayPartialDebtRequest request = new PayPartialDebtRequest(
                UUID.randomUUID(), null, LocalDate.now(), BigDecimal.TEN, null, null
        );

        ResponseEntity<PaymentResultResponse> result = paymentController.payPartialDebt(customerId, request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(payPartialDebtUseCase).execute(eq(customerId), any(PayPartialDebtRequest.class), any(UUID.class), anyString());
    }

    @Test
    @DisplayName("should update payment and return 200")
    void shouldUpdatePayment() {
        UUID id = UUID.randomUUID();
        PaymentResponse response = mock(PaymentResponse.class);

        when(updatePaymentUseCase.execute(eq(id), any(UpdatePaymentRequest.class)))
                .thenReturn(response);

        UpdatePaymentRequest request = new UpdatePaymentRequest("REF-UPDATED", null, null, null);

        ResponseEntity<PaymentResponse> result = paymentController.update(id, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(updatePaymentUseCase).execute(eq(id), any(UpdatePaymentRequest.class));
    }

    @Test
    @DisplayName("should delete payment and return 204")
    void shouldDeletePayment() {
        UUID id = UUID.randomUUID();
        doNothing().when(deletePaymentUseCase).execute(id);

        ResponseEntity<Void> result = paymentController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(deletePaymentUseCase).execute(id);
    }
}
