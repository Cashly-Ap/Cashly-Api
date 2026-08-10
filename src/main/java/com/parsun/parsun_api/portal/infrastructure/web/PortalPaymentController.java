package com.parsun.parsun_api.portal.infrastructure.web;

import com.parsun.parsun_api.payment_methods.application.dto.response.PaymentMethodResponse;
import com.parsun.parsun_api.payment_methods.domain.entities.PaymentMethod;
import com.parsun.parsun_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.parsun.parsun_api.payments.application.dto.response.PaymentListResponse;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResponse;
import com.parsun.parsun_api.payments.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalDebtRequest;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalInvoiceRequest;
import com.parsun.parsun_api.portal.application.dto.request.PayPortalPartialDebtRequest;
import com.parsun.parsun_api.portal.application.dto.request.RegisterPortalPaymentRequest;
import com.parsun.parsun_api.portal.application.usecases.GetPortalPaymentUseCase;
import com.parsun.parsun_api.portal.application.usecases.ListPortalPaymentsUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalAllDebtUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalInvoiceUseCase;
import com.parsun.parsun_api.portal.application.usecases.PayPortalPartialDebtUseCase;
import com.parsun.parsun_api.portal.application.usecases.RegisterPortalPaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal")
public class PortalPaymentController {

    private final ListPortalPaymentsUseCase listPortalPaymentsUseCase;
    private final GetPortalPaymentUseCase getPortalPaymentUseCase;
    private final PayPortalInvoiceUseCase payPortalInvoiceUseCase;
    private final PayPortalAllDebtUseCase payPortalAllDebtUseCase;
    private final PayPortalPartialDebtUseCase payPortalPartialDebtUseCase;
    private final RegisterPortalPaymentUseCase registerPortalPaymentUseCase;
    private final PaymentMethodRepository paymentMethodRepository;

    public PortalPaymentController(ListPortalPaymentsUseCase listPortalPaymentsUseCase,
                                    GetPortalPaymentUseCase getPortalPaymentUseCase,
                                    PayPortalInvoiceUseCase payPortalInvoiceUseCase,
                                    PayPortalAllDebtUseCase payPortalAllDebtUseCase,
                                    PayPortalPartialDebtUseCase payPortalPartialDebtUseCase,
                                    RegisterPortalPaymentUseCase registerPortalPaymentUseCase,
                                    PaymentMethodRepository paymentMethodRepository) {
        this.listPortalPaymentsUseCase = listPortalPaymentsUseCase;
        this.getPortalPaymentUseCase = getPortalPaymentUseCase;
        this.payPortalInvoiceUseCase = payPortalInvoiceUseCase;
        this.payPortalAllDebtUseCase = payPortalAllDebtUseCase;
        this.payPortalPartialDebtUseCase = payPortalPartialDebtUseCase;
        this.registerPortalPaymentUseCase = registerPortalPaymentUseCase;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentListResponse>> listPayments(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        List<PaymentListResponse> response = listPortalPaymentsUseCase.execute(
                dateFrom, dateTo, type, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        PaymentResponse response = getPortalPaymentUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<PaymentResultResponse> payInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody PayPortalInvoiceRequest request) {
        PaymentResultResponse response = payPortalInvoiceUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/debt/pay")
    public ResponseEntity<PaymentResultResponse> payAllDebt(
            @Valid @RequestBody PayPortalDebtRequest request) {
        PaymentResultResponse response = payPortalAllDebtUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/debt/pay-partial")
    public ResponseEntity<PaymentResultResponse> payPartialDebt(
            @Valid @RequestBody PayPortalPartialDebtRequest request) {
        PaymentResultResponse response = payPortalPartialDebtUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/payments/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaymentResponse> registerPayment(
            @Valid @RequestPart("request") RegisterPortalPaymentRequest request,
            @RequestPart(value = "justificante", required = false) MultipartFile justificante) {
        PaymentResponse response = registerPortalPaymentUseCase.execute(request, justificante);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethodResponse>> listPaymentMethods() {
        List<PaymentMethod> methods = paymentMethodRepository.findAll(true);
        List<PaymentMethodResponse> response = methods.stream()
                .map(m -> new PaymentMethodResponse(
                        m.getId().value(),
                        m.getCode(),
                        m.getName(),
                        m.getDescription(),
                        m.isActive(),
                        false,
                        m.getCreatedAt(),
                        m.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(response);
    }
}
