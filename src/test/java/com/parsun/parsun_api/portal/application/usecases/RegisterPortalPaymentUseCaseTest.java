package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.notifications.application.services.NotificationService;
import com.parsun.parsun_api.payments.application.dto.request.CreatePaymentRequest;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.usecases.CreatePaymentUseCase;
import com.parsun.parsun_api.portal.application.dto.request.RegisterPortalPaymentRequest;
import com.parsun.parsun_api.portal.application.services.PortalAuthorizationService;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterPortalPaymentUseCase Unit Tests")
class RegisterPortalPaymentUseCaseTest {

    @Mock
    private CreatePaymentUseCase createPaymentUseCase;

    @Mock
    private PortalAuthorizationService portalAuthorizationService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<CreatePaymentRequest> requestCaptor;

    private RegisterPortalPaymentUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegisterPortalPaymentUseCase(createPaymentUseCase, portalAuthorizationService,
                fileStorageService, notificationService, customerRepository);
        when(portalAuthorizationService.getCurrentCustomerId()).thenReturn(customerId);
        when(customerRepository.findById(new CustomerId(customerId))).thenReturn(Optional.of(mock(Customer.class)));
        when(createPaymentUseCase.execute(any(), isNull())).thenReturn(stubbedResponse());
    }

    private RegisterPortalPaymentRequest request() {
        return new RegisterPortalPaymentRequest(new BigDecimal("500.00"), paymentMethodId,
                LocalDate.now(), "REF-001", "Some notes");
    }

    private PaymentResponse stubbedResponse() {
        return new PaymentResponse(paymentId, "2026-0001", customerId, new BigDecimal("500.00"),
                paymentMethodId, "REF-001", LocalDate.now(), null, "PENDING", BigDecimal.ZERO,
                null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void should_SetVoucherPath_When_JustificanteFileProvided() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        when(fileStorageService.store(any(), any(), any())).thenReturn("payments/justificantes/stored.pdf");

        useCase.execute(request(), justificante);

        verify(createPaymentUseCase).execute(requestCaptor.capture(), isNull());
        assertEquals("payments/justificantes/stored.pdf", requestCaptor.getValue().voucherPath());
    }

    @Test
    void should_NotContainJustificanteTextInNotes_When_FileProvided() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        when(fileStorageService.store(any(), any(), any())).thenReturn("payments/justificantes/stored.pdf");

        useCase.execute(request(), justificante);

        verify(createPaymentUseCase).execute(requestCaptor.capture(), isNull());
        String notes = requestCaptor.getValue().notes();
        assertFalse(notes != null && notes.contains("Justificante:"));
    }

    @Test
    void should_KeepOriginalNotes_When_FileProvided() {
        MultipartFile justificante = new MockMultipartFile("justificante", "receipt.pdf",
                "application/pdf", "data".getBytes());
        when(fileStorageService.store(any(), any(), any())).thenReturn("payments/justificantes/stored.pdf");

        useCase.execute(request(), justificante);

        verify(createPaymentUseCase).execute(requestCaptor.capture(), isNull());
        assertEquals("Some notes", requestCaptor.getValue().notes());
    }

    @Test
    void should_LeaveVoucherPathNull_When_NoFileProvided() {
        useCase.execute(request(), null);

        verify(createPaymentUseCase).execute(requestCaptor.capture(), isNull());
        assertNull(requestCaptor.getValue().voucherPath());
        verify(fileStorageService, never()).store(any(), any(), any());
    }

    @Test
    void should_LeaveTargetInvoiceIdNull_When_RegisteringPayment() {
        useCase.execute(request(), null);

        verify(createPaymentUseCase).execute(requestCaptor.capture(), isNull());
        assertNull(requestCaptor.getValue().targetInvoiceId());
    }
}
