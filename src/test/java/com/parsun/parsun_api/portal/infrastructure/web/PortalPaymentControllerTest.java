package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payments.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalDebtRequest;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalInvoiceRequest;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalPartialDebtRequest;
import com.parsun.parsun_api.portal.application.dto.request.RegisterPortalPaymentRequest;
import com.parsun.parsun_api.portal.application.usecases.GetPendingPortalPaymentsUseCase;
import com.parsun.parsun_api.portal.application.usecases.GetPortalPaymentUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalPaymentsUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalAllDebtUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalInvoiceUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalPartialDebtUseCase;
import com.parsun.parsun_api.portal.application.usecases.RegisterPortalPaymentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortalPaymentController Unit Tests")
class PortalPaymentControllerTest {

    @Mock
    private ListPortalPaymentsUseCase listPortalPaymentsUseCase;

    @Mock
    private GetPortalPaymentUseCase getPortalPaymentUseCase;

    @Mock
    private PayPortalInvoiceUseCase payPortalInvoiceUseCase;

    @Mock
    private PayPortalAllDebtUseCase payPortalAllDebtUseCase;

    @Mock
    private PayPortalPartialDebtUseCase payPortalPartialDebtUseCase;

    @Mock
    private RegisterPortalPaymentUseCase registerPortalPaymentUseCase;

    @Mock
    private GetPendingPortalPaymentsUseCase getPendingPortalPaymentsUseCase;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Captor
    private ArgumentCaptor<MultipartFile> fileCaptor;

    @InjectMocks
    private PortalPaymentController controller;

    private final UUID invoiceId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    private PaymentResponse stubbedResponse() {
        return new PaymentResponse(paymentId, "2026-0001", UUID.randomUUID(), new BigDecimal("500.00"),
                paymentMethodId, "REF-001", LocalDate.now(), null, "PENDING", BigDecimal.ZERO,
                null, null, LocalDateTime.now(), LocalDateTime.now(), null, null, null, null,
                invoiceId, null);
    }

    @Test
    void should_ReturnCreated_When_PayingInvoiceWithFile() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        PayPortalInvoiceRequest request = new PayPortalInvoiceRequest(paymentMethodId, "REF-001",
                LocalDate.now(), new BigDecimal("500.00"), false, null);
        when(payPortalInvoiceUseCase.execute(eq(invoiceId), eq(request), any(MultipartFile.class)))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.payInvoice(invoiceId, request, justificante);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(payPortalInvoiceUseCase).execute(invoiceId, request, justificante);
    }

    @Test
    void should_ReturnCreated_When_PayingInvoiceWithoutFile() {
        PayPortalInvoiceRequest request = new PayPortalInvoiceRequest(paymentMethodId, "REF-001",
                LocalDate.now(), new BigDecimal("500.00"), false, null);
        when(payPortalInvoiceUseCase.execute(eq(invoiceId), eq(request), isNull()))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.payInvoice(invoiceId, request, null);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(payPortalInvoiceUseCase).execute(invoiceId, request, null);
    }

    @Test
    void should_ReturnCreated_When_PayingAllDebtWithFile() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        PayPortalDebtRequest request = new PayPortalDebtRequest(paymentMethodId, "REF-001",
                LocalDate.now(), new BigDecimal("500.00"), false, null);
        when(payPortalAllDebtUseCase.execute(eq(request), any(MultipartFile.class)))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.payAllDebt(request, justificante);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(payPortalAllDebtUseCase).execute(request, justificante);
    }

    @Test
    void should_ReturnCreated_When_PayingAllDebtWithoutFile() {
        PayPortalDebtRequest request = new PayPortalDebtRequest(paymentMethodId, "REF-001",
                LocalDate.now(), new BigDecimal("500.00"), false, null);
        when(payPortalAllDebtUseCase.execute(eq(request), isNull()))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.payAllDebt(request, null);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(payPortalAllDebtUseCase).execute(request, null);
    }

    @Test
    void should_ReturnCreated_When_PayingPartialDebtWithFile() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        PayPortalPartialDebtRequest request = new PayPortalPartialDebtRequest(paymentMethodId, "REF-001",
                LocalDate.now(), new BigDecimal("250.00"), false, null);
        when(payPortalPartialDebtUseCase.execute(eq(request), any(MultipartFile.class)))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.payPartialDebt(request, justificante);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(payPortalPartialDebtUseCase).execute(request, justificante);
    }

    @Test
    void should_ReturnCreated_When_PayingPartialDebtWithoutFile() {
        PayPortalPartialDebtRequest request = new PayPortalPartialDebtRequest(paymentMethodId, "REF-001",
                LocalDate.now(), new BigDecimal("250.00"), false, null);
        when(payPortalPartialDebtUseCase.execute(eq(request), isNull()))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.payPartialDebt(request, null);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(payPortalPartialDebtUseCase).execute(request, null);
    }

    @Test
    void should_ReturnCreated_When_RegisteringPaymentWithFile() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        RegisterPortalPaymentRequest request = new RegisterPortalPaymentRequest(
                new BigDecimal("500.00"), paymentMethodId, LocalDate.now(), "REF-001", null);
        when(registerPortalPaymentUseCase.execute(eq(request), any(MultipartFile.class)))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.registerPayment(request, justificante);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(registerPortalPaymentUseCase).execute(request, justificante);
    }

    @Test
    void should_ReturnCreated_When_RegisteringPaymentWithoutFile() {
        RegisterPortalPaymentRequest request = new RegisterPortalPaymentRequest(
                new BigDecimal("500.00"), paymentMethodId, LocalDate.now(), "REF-001", null);
        when(registerPortalPaymentUseCase.execute(eq(request), isNull()))
                .thenReturn(stubbedResponse());

        ResponseEntity<PaymentResponse> result = controller.registerPayment(request, null);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(registerPortalPaymentUseCase).execute(request, null);
    }

    @Test
    void should_ReturnPendingPayments_When_QueryingWithInvoiceId() {
        PaymentListResponse listResponse = new PaymentListResponse(paymentId, "2026-0001", UUID.randomUUID(),
                new BigDecimal("500.00"), UUID.randomUUID(), "REF-001", LocalDate.now(), "PENDING", BigDecimal.ZERO,
                LocalDateTime.now(), invoiceId, null);
        when(getPendingPortalPaymentsUseCase.execute(invoiceId)).thenReturn(List.of(listResponse));

        ResponseEntity<List<PaymentListResponse>> result = controller.pendingCheck(invoiceId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void should_ReturnPendingPayments_When_QueryingWithoutInvoiceId() {
        when(getPendingPortalPaymentsUseCase.execute(null)).thenReturn(List.of());

        ResponseEntity<List<PaymentListResponse>> result = controller.pendingCheck(null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(getPendingPortalPaymentsUseCase).execute(null);
    }
}
