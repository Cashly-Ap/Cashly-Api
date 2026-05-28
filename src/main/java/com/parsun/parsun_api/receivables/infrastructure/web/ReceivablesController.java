package com.parsun.parsun_api.receivables.infrastructure.web;

import com.parsun.parsun_api.receivables.application.dto.request.ApprovePaymentRequest;
import com.parsun.parsun_api.receivables.application.dto.request.PayCustomerDebtRequest;
import com.parsun.parsun_api.receivables.application.dto.request.PayInvoiceRequest;
import com.parsun.parsun_api.receivables.application.dto.request.PayPartialDebtRequest;
import com.parsun.parsun_api.receivables.application.dto.request.RejectPaymentRequest;
import com.parsun.parsun_api.receivables.application.dto.response.CustomerDebtSummaryResponse;
import com.parsun.parsun_api.receivables.application.dto.response.PaymentResultResponse;
import com.parsun.parsun_api.receivables.application.dto.response.PendingPaymentResponse;
import com.parsun.parsun_api.receivables.application.dto.response.ReceivablesSummaryResponse;
import com.parsun.parsun_api.receivables.application.usecases.*;
import com.parsun.parsun_api.receivables.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.shared.storage.domain.FileNotFoundException;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
public class ReceivablesController {

    private final GetReceivablesSummaryUseCase getReceivablesSummaryUseCase;
    private final SearchCustomerDebtUseCase searchCustomerDebtUseCase;
    private final GetCustomerDebtUseCase getCustomerDebtUseCase;
    private final GetCustomerInvoicesUseCase getCustomerInvoicesUseCase;
    private final PayInvoiceUseCase payInvoiceUseCase;
    private final PayAllCustomerDebtUseCase payAllCustomerDebtUseCase;
    private final PayPartialDebtUseCase payPartialDebtUseCase;
    private final GetPendingPaymentsUseCase getPendingPaymentsUseCase;
    private final ApprovePaymentUseCase approvePaymentUseCase;
    private final RejectPaymentUseCase rejectPaymentUseCase;
    private final GetPaymentHistoryUseCase getPaymentHistoryUseCase;
    private final ReceivablesRepository receivablesRepository;
    private final FileStorageService fileStorageService;

    public ReceivablesController(GetReceivablesSummaryUseCase getReceivablesSummaryUseCase,
                                  SearchCustomerDebtUseCase searchCustomerDebtUseCase,
                                  GetCustomerDebtUseCase getCustomerDebtUseCase,
                                  GetCustomerInvoicesUseCase getCustomerInvoicesUseCase,
                                  PayInvoiceUseCase payInvoiceUseCase,
                                  PayAllCustomerDebtUseCase payAllCustomerDebtUseCase,
                                  PayPartialDebtUseCase payPartialDebtUseCase,
                                  GetPendingPaymentsUseCase getPendingPaymentsUseCase,
                                  ApprovePaymentUseCase approvePaymentUseCase,
                                  RejectPaymentUseCase rejectPaymentUseCase,
                                  GetPaymentHistoryUseCase getPaymentHistoryUseCase,
                                  ReceivablesRepository receivablesRepository,
                                  FileStorageService fileStorageService) {
        this.getReceivablesSummaryUseCase = getReceivablesSummaryUseCase;
        this.searchCustomerDebtUseCase = searchCustomerDebtUseCase;
        this.getCustomerDebtUseCase = getCustomerDebtUseCase;
        this.getCustomerInvoicesUseCase = getCustomerInvoicesUseCase;
        this.payInvoiceUseCase = payInvoiceUseCase;
        this.payAllCustomerDebtUseCase = payAllCustomerDebtUseCase;
        this.payPartialDebtUseCase = payPartialDebtUseCase;
        this.getPendingPaymentsUseCase = getPendingPaymentsUseCase;
        this.approvePaymentUseCase = approvePaymentUseCase;
        this.rejectPaymentUseCase = rejectPaymentUseCase;
        this.getPaymentHistoryUseCase = getPaymentHistoryUseCase;
        this.receivablesRepository = receivablesRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<ReceivablesSummaryResponse> getSummary() {
        ReceivablesSummaryResponse response = getReceivablesSummaryUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getCustomersWithDebt(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = searchCustomerDebtUseCase.execute(search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<CustomerDebtSummaryResponse> getCustomerDebt(@PathVariable UUID id) {
        CustomerDebtSummaryResponse response = getCustomerDebtUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{id}/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getCustomerInvoices(
            @PathVariable UUID id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = getCustomerInvoicesUseCase.execute(id, search, status, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invoices/{id}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payInvoice(
            @PathVariable UUID id,
            @RequestPart("request") @Valid PayInvoiceRequest request,
            @RequestPart(value = "voucher", required = false) MultipartFile voucherFile,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payInvoiceUseCase.execute(id, request, voucherFile, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/customers/{id}/pay-all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payAllCustomerDebt(
            @PathVariable UUID id,
            @RequestPart("request") @Valid PayCustomerDebtRequest request,
            @RequestPart(value = "voucher", required = false) MultipartFile voucherFile,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payAllCustomerDebtUseCase.execute(id, request, voucherFile, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/customers/{id}/pay-partial")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PaymentResultResponse> payPartialDebt(
            @PathVariable UUID id,
            @RequestPart("request") @Valid PayPartialDebtRequest request,
            @RequestPart(value = "voucher", required = false) MultipartFile voucherFile,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        String userName = authentication.getName();
        PaymentResultResponse response = payPartialDebtUseCase.execute(id, request, voucherFile, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/payments/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getPendingPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = getPendingPaymentsUseCase.execute(status, search, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/payments/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> approvePayment(
            @PathVariable UUID id,
            @RequestBody ApprovePaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        approvePaymentUseCase.execute(id, request, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/payments/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<Void> rejectPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RejectPaymentRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        rejectPaymentUseCase.execute(id, request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payments/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<PageResponse<?>> getPaymentHistory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<?> response = getPaymentHistoryUseCase.execute(search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{id}/proof")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_FINANCES')")
    public ResponseEntity<byte[]> getPaymentProof(@PathVariable UUID id) {
        var paymentOpt = receivablesRepository.findPendingPaymentById(id);
        if (paymentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var payment = paymentOpt.get();
        if (payment.voucherPath() == null || payment.voucherPath().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = fileStorageService.load(payment.voucherPath());
            String contentType = detectContentType(payment.voucherPath());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(content);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String detectContentType(String filename) {
        if (filename == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF_VALUE;
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
